package com.gametout.gametout.verification;
import com.gametout.gametout.dto.AuthenticatedUser;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class EmailVerificationAspect {

    @Before("@annotation(EmailVerifiedRequired)")
    public void checkEmailVerification() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

        if (!user.getUser().isEmailVerified()) {
            throw new AccessDeniedException("Email not verified");
        }
    }
}
