package com.vehiclecare.vehiclecaremicro.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vehiclecare.vehiclecaremicro.infrastructure.external.vpic.VpicClient;
import com.vehiclecare.vehiclecaremicro.infrastructure.external.vpic.VpicMakeDTO;
import com.vehiclecare.vehiclecaremicro.infrastructure.external.vpic.VpicModelDTO;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.BrandEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.ModelEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.BrandJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.ModelJpaRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CatalogSyncServiceTest {

    @Mock
    private VpicClient vpicClient;
    @Mock
    private BrandJpaRepository brandJpaRepository;
    @Mock
    private ModelJpaRepository modelJpaRepository;

    private CatalogSyncService service;

    @BeforeEach
    void setUp() {
        service = new CatalogSyncService(vpicClient, brandJpaRepository, modelJpaRepository);
    }

    @AfterEach
    void unlockIfNeeded() throws Exception {
        ReentrantLock lock = syncLock();
        while (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    @Test
    void syncCatalog_skipsWhenAnotherSyncIsRunning() throws Exception {
        ReentrantLock lock = syncLock();
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            lock.lock();
            try {
                locked.countDown();
                release.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        });
        thread.start();
        locked.await();

        try {
            service.syncCatalog();
        } finally {
            release.countDown();
            thread.join();
        }

        verify(vpicClient, never()).fetchMakes();
    }

    @Test
    void syncCatalog_returnsWhenNoMakesReceived() {
        when(vpicClient.fetchMakes()).thenReturn(List.of());

        service.syncCatalog();

        verify(brandJpaRepository, never()).save(any());
    }

    @Test
    void syncCatalog_savesAllowedBrandsAndDeactivatesMissingOnes() {
        VpicMakeDTO allowedMake = make("10", "BMW");
        VpicMakeDTO allowedPrefixedMake = make("11", "Mercedes-Benz AMG");
        VpicMakeDTO ignoredNull = make(null, "Audi");
        VpicMakeDTO ignoredDisallowed = make("12", "Unknown Brand");
        BrandEntity existingActive = BrandEntity.builder().id("stale").name("Old").active(true).build();
        ModelEntity staleModel = ModelEntity.builder().id("m-stale").brand(existingActive).name("Old model").active(true).build();

        when(vpicClient.fetchMakes()).thenReturn(List.of(allowedMake, allowedPrefixedMake, ignoredNull, ignoredDisallowed));
        when(brandJpaRepository.findAll()).thenReturn(List.of(existingActive));
        when(brandJpaRepository.save(any(BrandEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelJpaRepository.findByBrand_Id("10")).thenReturn(List.of());
        when(modelJpaRepository.findByBrand_Id("11")).thenReturn(List.of());
        when(vpicClient.fetchModelsForMake("10")).thenReturn(List.of());
        when(vpicClient.fetchModelsForMake("11")).thenReturn(List.of());
        when(modelJpaRepository.findByBrand_Id("stale")).thenReturn(List.of(staleModel));

        service.syncCatalog();

        ArgumentCaptor<BrandEntity> brandCaptor = ArgumentCaptor.forClass(BrandEntity.class);
        verify(brandJpaRepository, org.mockito.Mockito.atLeast(3)).save(brandCaptor.capture());
        List<BrandEntity> savedBrands = brandCaptor.getAllValues();
        assertTrue(savedBrands.stream().anyMatch(brand -> "10".equals(brand.getId()) && brand.isActive() && "BMW".equals(brand.getName())));
        assertTrue(savedBrands.stream().anyMatch(brand -> "11".equals(brand.getId()) && brand.isActive()));
        assertTrue(savedBrands.stream().anyMatch(brand -> "stale".equals(brand.getId()) && !brand.isActive()));
        assertFalse(staleModel.isActive());
        verify(modelJpaRepository).saveAll(List.of(staleModel));
    }

    @Test
    void syncCatalog_skipsBrandWhenModelSyncThrowsLockException() {
        CatalogSyncService spyService = spy(service);
        VpicMakeDTO make = make("10", "BMW");
        when(vpicClient.fetchMakes()).thenReturn(List.of(make));
        when(brandJpaRepository.findAll()).thenReturn(List.of());
        when(brandJpaRepository.save(any(BrandEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new PessimisticLockingFailureException("lock")).when(spyService).syncModelsForBrand(any(BrandEntity.class), eq("10"));

        spyService.syncCatalog();

        verify(spyService).syncModelsForBrand(any(BrandEntity.class), eq("10"));
    }

    @Test
    void scheduledSync_skipsWhenDisabled() {
        ReflectionTestUtils.setField(service, "catalogSyncEnabled", false);

        service.scheduledSync();

        verify(vpicClient, never()).fetchMakes();
    }

    @Test
    void scheduledSync_runsWhenEnabled() {
        CatalogSyncService spyService = spy(service);
        ReflectionTestUtils.setField(spyService, "catalogSyncEnabled", true);
        doNothing().when(spyService).syncCatalog();

        spyService.scheduledSync();

        verify(spyService).syncCatalog();
    }

    @Test
    void syncModelsForBrand_savesAndDeactivatesModels() {
        BrandEntity brand = BrandEntity.builder().id("b1").name("BMW").active(true).build();
        ModelEntity existing = ModelEntity.builder().id("m1").brand(brand).name("Old").active(false).build();
        ModelEntity stale = ModelEntity.builder().id("m2").brand(brand).name("Stale").active(true).build();
        VpicModelDTO updated = model("m1", "320i", "b1");
        VpicModelDTO created = model("m3", "330i", "b1");
        VpicModelDTO ignored = model(null, "Ignored", "b1");
        when(modelJpaRepository.findByBrand_Id("b1")).thenReturn(List.of(existing, stale));
        when(vpicClient.fetchModelsForMake("mk1")).thenReturn(List.of(updated, created, ignored));
        when(modelJpaRepository.save(any(ModelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.syncModelsForBrand(brand, "mk1");

        assertEquals("320i", existing.getName());
        assertTrue(existing.isActive());
        assertFalse(stale.isActive());
        verify(modelJpaRepository).save(existing);
        verify(modelJpaRepository).saveAll(List.of(existing, stale));
    }

    @Test
    void normalizeAndAllowedBrandHandleAccentsPrefixesAndNulls() {
        assertEquals("", ReflectionTestUtils.invokeMethod(service, "normalize", new Object[] {null}));
        assertEquals("citroen", ReflectionTestUtils.invokeMethod(service, "normalize", "Citroën"));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(service, "isAllowedBrand", "DS Automobiles Crossback"));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(service, "isAllowedBrand", "Random"));
    }

    private static VpicMakeDTO make(String id, String name) {
        VpicMakeDTO make = new VpicMakeDTO();
        make.setId(id);
        make.setName(name);
        return make;
    }

    private static VpicModelDTO model(String id, String name, String makeId) {
        VpicModelDTO model = new VpicModelDTO();
        model.setId(id);
        model.setName(name);
        model.setMakeId(makeId);
        return model;
    }

    private static ReentrantLock syncLock() throws Exception {
        Field field = CatalogSyncService.class.getDeclaredField("SYNC_LOCK");
        field.setAccessible(true);
        return (ReentrantLock) field.get(null);
    }
}
