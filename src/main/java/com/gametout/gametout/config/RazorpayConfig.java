package com.gametout.gametout.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "razorpay")
@Data
public class RazorpayConfig {

    private String keyId;
    private String keySecret;
    private String webhookSecret;
    private Plans plans = new Plans();

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }

    @Data
    public static class Plans {
        private Plan viewer = new Plan();
        private Plan creator = new Plan();
    }

    @Data
    public static class Plan {
        private Integer amount;         // in paise
        private Integer durationMonths = 12;
        private String name;
        private String description;
    }

    /**
     * Get plan configuration by subscription type
     */
    public Plan getPlan(com.gametout.gametout.enums.SubscriptionType type) {
        return switch (type) {
            case VIEWER -> plans.getViewer();
            case CREATOR -> plans.getCreator();
        };
    }
}
