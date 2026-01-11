package com.gametout.gametout.controller;
import com.gametout.gametout.verification.EmailVerifiedRequired;
import com.gametout.gametout.dto.PortfolioRequest;
import com.gametout.gametout.entity.PortfolioProfile;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.enums.JobCategory;
import com.gametout.gametout.dto.AuthenticatedUser;
import com.gametout.gametout.dto.PortfolioPageResponse;
import com.gametout.gametout.service.PortfolioService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService service;

    public PortfolioController(PortfolioService service) {
        this.service = service;
    }

    @PostMapping
    @EmailVerifiedRequired
    public PortfolioProfile createOrUpdate(
        Authentication auth,
        @RequestBody PortfolioRequest req
    ) {
        UserAccount user =
            ((AuthenticatedUser) auth.getPrincipal()).getUser();
        return service.createOrUpdate(user, req);
    }
    

    @GetMapping("/list")
    @Cacheable(
        value = "portfolio:list",
        key = "#category + ':' + #pageable.pageNumber"
    )
    public PortfolioPageResponse list(
        @RequestParam JobCategory category,
        Pageable pageable
    ) {
        return service.list(category, pageable);
    }

    @GetMapping("/user/premium")
    public PortfolioPageResponse getPremiumUsers(Authentication authuser, Pageable pageable) {
        return service.getPremiumPortfolios(authuser, pageable);
    }

    @PostMapping("/{id}/like")
    public void like(@PathVariable Long id, Authentication auth) {
        UserAccount user =
            ((AuthenticatedUser) auth.getPrincipal()).getUser();
        service.like(id,user);
    }
}
