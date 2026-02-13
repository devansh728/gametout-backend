package com.gametout.gametout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for initiating OAuth2 authorization.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2AuthorizationRequest {
    private String authorizationUrl;
    private String state;
}
