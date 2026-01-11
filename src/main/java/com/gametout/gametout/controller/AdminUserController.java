package com.gametout.gametout.controller;
import com.gametout.gametout.enums.UserRole;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.repository.UserAccountRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserAccountRepository repo;

    public AdminUserController(UserAccountRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/{id}/upgrade")
    public void upgrade(@PathVariable Long id) {
        UserAccount user = repo.findById(id).orElseThrow();
        user.setRole(UserRole.PREMIUM);
    }

    @PostMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id) {
        UserAccount user = repo.findById(id).orElseThrow();
        user.setActive(false);
    }
}

