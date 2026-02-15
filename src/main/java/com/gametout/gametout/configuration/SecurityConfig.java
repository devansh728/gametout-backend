package com.gametout.gametout.configuration;

import com.gametout.gametout.filter.FirebaseAuthenticationFilter;
import com.gametout.gametout.filter.OAuth2AuthenticationFilter;

import java.util.Arrays;

import java.util.List;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final FirebaseAuthenticationFilter firebaseFilter;
        private final OAuth2AuthenticationFilter oauth2Filter;

        public SecurityConfig(
                FirebaseAuthenticationFilter firebaseFilter,
                OAuth2AuthenticationFilter oauth2Filter
        ) {
                this.firebaseFilter = firebaseFilter;
                this.oauth2Filter = oauth2Filter;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .headers(headers -> headers
                                                .xssProtection(xss -> xss
                                                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED))
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives(
                                                                                "default-src 'self'; frame-ancestors 'none';"))
                                                .frameOptions(frame -> frame.deny()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                // Media presign endpoints are public - anyone can get presigned URLs
                                                .requestMatchers("/api/media/presign/**").permitAll()
                                                .requestMatchers("/api/media/**").hasAnyRole("ADMIN", "USER","PREMIUM")
                                                .requestMatchers("/api/premium/**").hasAnyRole("PREMIUM", "ADMIN")
                                                // OAuth2 endpoints (public)
                                                .requestMatchers("/api/oauth2/authorize/**").permitAll()
                                                .requestMatchers("/api/oauth2/login/**").permitAll()
                                                .requestMatchers("/api/oauth2/callback/**").permitAll()
                                                // OAuth2 endpoints (authenticated)
                                                .requestMatchers("/api/oauth2/link/**").authenticated()
                                                .requestMatchers("/api/oauth2/unlink/**").authenticated()
                                                .requestMatchers("/api/oauth2/linked-accounts").authenticated()
                                                // Payment endpoints
                                                .requestMatchers(HttpMethod.POST, "/api/payment/webhook").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/payment/**").authenticated()
                                                // Subscription endpoints
                                                .requestMatchers("/api/user/subscription/**").authenticated()
                                                // Portfolio endpoints
                                                .requestMatchers(HttpMethod.POST, "/api/portfolio/**").authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/portfolio/my").authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/portfolio/can-view-full").authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/portfolio/{id}/is-owner").authenticated()
                                                // Studio rating endpoints
                                                .requestMatchers(HttpMethod.POST, "/api/user/studio/{id}/rate").authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/user/studio/{id}/rating").authenticated()
                                                .requestMatchers("/api/auth/**").authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/posts/{id}").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/posts/type/{type}").permitAll()
                                                .requestMatchers("/api/posts/{id}/like").authenticated()
                                                // .requestMatchers(HttpMethod.POST, "/api/featured/{type}").authenticated()
                                                .requestMatchers(HttpMethod.GET,"/api/headlines").permitAll()
                                                .requestMatchers(HttpMethod.POST,"/api/headlines").authenticated()
                                                .requestMatchers(HttpMethod.DELETE,"/api/headlines/**").authenticated()
                                                .anyRequest().permitAll())
                                // Firebase filter runs before UsernamePasswordAuthenticationFilter
                                .addFilterBefore(
                                                firebaseFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                // OAuth2 JWT filter runs BEFORE Firebase filter (so it processes first)
                                .addFilterBefore(
                                                oauth2Filter,
                                                firebaseFilter.getClass());

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("*")); // Restrict in production
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setExposedHeaders(List.of("Authorization"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}

// Prevent clickjacking
// .httpStrictTransportSecurity(hsts -> hsts // Enforce HTTPS
// .includeSubDomains(true)
// .maxAgeInSeconds(31536000) // 1 year
// )