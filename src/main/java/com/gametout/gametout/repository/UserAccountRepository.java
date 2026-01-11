package com.gametout.gametout.repository;
import com.gametout.gametout.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);
}
