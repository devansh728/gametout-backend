package com.gametout.gametout.configuration;

import com.gametout.gametout.filter.FirebaseAuthenticationFilter;

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

        public SecurityConfig(FirebaseAuthenticationFilter firebaseFilter) {
                this.firebaseFilter = firebaseFilter;
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
                                                .requestMatchers("/api/media/**").hasAnyRole("ADMIN", "USER")
                                                .requestMatchers("/api/premium/**").hasAnyRole("PREMIUM", "ADMIN")
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
                                .addFilterBefore(
                                                firebaseFilter,
                                                UsernamePasswordAuthenticationFilter.class);

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