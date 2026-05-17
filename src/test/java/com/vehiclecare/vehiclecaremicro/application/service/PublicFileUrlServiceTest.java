package com.vehiclecare.vehiclecaremicro.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class PublicFileUrlServiceTest {

    private PublicFileUrlService service;

    @BeforeEach
    void setUp() {
        service = new PublicFileUrlService("vehiclecare");
    }

    @Test
    void buildObjectUrl_returnsNullWhenReferenceMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/1");

        assertNull(service.buildObjectUrl(request, null));
        assertNull(service.buildObjectUrl(request, " "));
        assertNull(service.buildObjectUrl(request, "///"));
    }

    @Test
    void buildObjectUrl_buildsPublicDownloadUrl() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/1");
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);

        String result = service.buildObjectUrl(request, "profiles/user-1/image.jpg");

        assertEquals("http://localhost:8080/files/object?key=profiles/user-1/image.jpg", result);
    }

    @Test
    void extractObjectKey_returnsNullWhenReferenceMissing() {
        assertNull(service.extractObjectKey(null));
        assertNull(service.extractObjectKey("   "));
    }

    @Test
    void extractObjectKey_returnsNormalizedRelativePath() {
        assertEquals("records/1/file.pdf", service.extractObjectKey("/records/1/file.pdf"));
    }

    @Test
    void extractObjectKey_stripsBucketPrefixFromAbsoluteUrl() {
        assertEquals(
                "records/1/file.pdf",
                service.extractObjectKey("http://localhost:9000/vehiclecare/records/1/file.pdf")
        );
    }

    @Test
    void extractObjectKey_keepsAbsolutePathWithoutBucketPrefix() {
        assertEquals(
                "other-bucket/records/1/file.pdf",
                service.extractObjectKey("http://localhost:9000/other-bucket/records/1/file.pdf")
        );
    }

    @Test
    void extractObjectKey_returnsNullWhenAbsoluteUrlHasNoPath() {
        assertNull(service.extractObjectKey("http://localhost:9000"));
    }

    @Test
    void extractObjectKey_returnsNullWhenAbsoluteUrlHasBlankPath() {
        assertNull(service.extractObjectKey("http://localhost:9000?download=true"));
    }

    @Test
    void extractObjectKey_treatsUriWithoutHostAsPlainReference() {
        assertEquals("file:///records/1/file.pdf", service.extractObjectKey("file:///records/1/file.pdf"));
    }

    @Test
    void extractObjectKey_normalizesPathWithMultipleLeadingSlashes() {
        assertEquals(
                "vehiclecare/records/1/file.pdf",
                service.extractObjectKey("///vehiclecare/records/1/file.pdf")
        );
    }
}
