package com.Satisfyre.app.repo;

import com.Satisfyre.app.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<UserEntity> findByReferralCode(String referralCode);
    List<UserEntity> findAllByReferredBy(String referralCode);



}
