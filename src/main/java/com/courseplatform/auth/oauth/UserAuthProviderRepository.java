package com.courseplatform.auth.oauth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthProviderRepository extends JpaRepository<UserAuthProviderEntity, Long> {

    @Query("SELECT p FROM UserAuthProviderEntity p JOIN FETCH p.user WHERE p.provider = :provider AND p.providerSubject = :providerSubject")
    Optional<UserAuthProviderEntity> findByProviderAndProviderSubject(@Param("provider") String provider, @Param("providerSubject") String providerSubject);

    boolean existsByProviderAndProviderSubject(String provider, String providerSubject);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserAuthProviderEntity p WHERE p.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
