package com.gametout.gametout.dto;

import com.gametout.gametout.enums.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a linked OAuth account for the user.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkedAccountDTO {
    private Long id;
    private AuthProvider provider;
    private String providerUsername;
    private String providerEmail;
    private String avatarUrl;
    private LocalDateTime linkedAt;
}
