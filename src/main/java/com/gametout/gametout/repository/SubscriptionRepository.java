package com.gametout.gametout.repository;

import com.gametout.gametout.entity.Subscription;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find subscription by user
     */
    Optional<Subscription> findByUser(UserAccount user);
    
    /**
     * Find subscription by user ID
     */
    Optional<Subscription> findByUserId(Long userId);
    
    /**
     * Find active subscription by user ID
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE' AND s.expiresAt > :now")
    Optional<Subscription> findActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    /**
     * Check if user has active subscription
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Subscription s " +
           "WHERE s.user.id = :userId AND s.status = 'ACTIVE' AND s.expiresAt > :now")
    boolean hasActiveSubscription(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    /**
     * Find all expired subscriptions that need to be updated
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.expiresAt < :now")
    List<Subscription> findExpiredSubscriptions(@Param("now") LocalDateTime now);
    
    /**
     * Find subscriptions expiring soon (for reminder emails)
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.expiresAt BETWEEN :start AND :end")
    List<Subscription> findExpiringSoon(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
