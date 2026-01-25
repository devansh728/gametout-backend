package com.gametout.gametout.repository;

import com.gametout.gametout.entity.PaymentTransaction;
import com.gametout.gametout.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    /**
     * Find by Razorpay order ID
     */
    Optional<PaymentTransaction> findByRazorpayOrderId(String razorpayOrderId);
    
    /**
     * Find by Razorpay payment ID
     */
    Optional<PaymentTransaction> findByRazorpayPaymentId(String razorpayPaymentId);
    
    /**
     * Find all transactions by user
     */
    Page<PaymentTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find transactions by user and status
     */
    List<PaymentTransaction> findByUserIdAndStatus(Long userId, PaymentStatus status);
    
    /**
     * Find successful payments by user
     */
    @Query("SELECT p FROM PaymentTransaction p WHERE p.user.id = :userId AND p.status = 'CAPTURED' ORDER BY p.paidAt DESC")
    List<PaymentTransaction> findSuccessfulPaymentsByUser(@Param("userId") Long userId);
    
    /**
     * Check if user has any successful payment
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PaymentTransaction p " +
           "WHERE p.user.id = :userId AND p.status = 'CAPTURED'")
    boolean hasSuccessfulPayment(@Param("userId") Long userId);
    
    /**
     * Find pending/created orders older than threshold (for cleanup)
     */
    @Query("SELECT p FROM PaymentTransaction p WHERE p.status = 'CREATED' AND p.createdAt < :threshold")
    List<PaymentTransaction> findStaleOrders(@Param("threshold") java.time.LocalDateTime threshold);
}
