package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.adapter;

import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.UserMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.UserEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;
    private final EntityManager entityManager;

    public UserPersistenceAdapter(UserJpaRepository userJpaRepository, UserMapper userMapper, EntityManager entityManager) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity saved = userJpaRepository.saveAndFlush(userMapper.toEntity(user));
        entityManager.refresh(saved);
        return userMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(String id) {
        return userJpaRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}
