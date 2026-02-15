package com.gametout.gametout.service;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.enums.UserRole;
import com.gametout.gametout.enums.SubscriptionType;
import com.gametout.gametout.repository.UserAccountRepository;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;


@Service
@Transactional
public class UserProvisioningService {

    private final UserAccountRepository repo;

    public UserProvisioningService(UserAccountRepository repo) {
        this.repo = repo;
    }

    @Cacheable(value = "user_entity", key = "#token.uid")
    public UserAccount getOrCreateUser(FirebaseToken token) {

        return repo.findByFirebaseUid(token.getUid())
            .orElseGet(() -> {
                UserAccount user = new UserAccount();
                user.setFirebaseUid(token.getUid());
                user.setEmail(token.getEmail());
                user.setEmailVerified(token.isEmailVerified());
                user.setRole(UserRole.PREMIUM);
                user.setSubscriptionType(SubscriptionType.CREATOR);
                user.setSubscriptionExpiresAt(java.time.LocalDateTime.now().plusYears(1));
                return repo.save(user);
            });
    }
}

