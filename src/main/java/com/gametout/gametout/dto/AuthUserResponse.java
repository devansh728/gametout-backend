package com.gametout.gametout.dto;

import com.gametout.gametout.enums.AuthProvider;
import com.gametout.gametout.enums.SubscriptionType;
import com.gametout.gametout.enums.UserRole;

public record AuthUserResponse(
        Long id,
        String email,
        UserRole role,
        boolean emailVerified,
        boolean active,
        SubscriptionType subscriptionType,
        AuthProvider authProvider
) {}

