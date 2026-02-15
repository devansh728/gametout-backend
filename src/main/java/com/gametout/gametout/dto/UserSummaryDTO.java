package com.gametout.gametout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Lightweight user summary for API responses.
 * Prevents leaking sensitive UserAccount fields (email, firebaseUid, role, subscription, etc.)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSummaryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String displayName;
}
