package com.gametout.gametout.configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
@Configuration
public class S3Config {

    @Value("${storage.endpoint}")
    private String endpoint;

    @Value("${storage.public-endpoint}")
    private String publicEndpoint;

    @Value("${storage.access-key}")
    private String accessKey;

    @Value("${storage.secret-key}")
    private String secretKey;

    @Value("${storage.region}")
    private String region;

    @Value("${storage.bucket}")
    private String bucket;

    /**
     * S3Presigner configured with PUBLIC endpoint for generating URLs
     * that the browser can access (e.g., http://localhost:9000)
     */
    @Bean
    public S3Presigner s3Presigner() {
        AwsCredentialsProvider credentials =
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            );
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true) 
                .checksumValidationEnabled(false)
                .build();
        
        return S3Presigner.builder()
                .credentialsProvider(credentials)
                .region(Region.of(region))
                .endpointOverride(URI.create(publicEndpoint))
                .serviceConfiguration(serviceConfiguration)
                .build();
    }

    /**
     * S3Client configured with INTERNAL endpoint for server-side
     * operations (e.g., http://minio:9000 in Docker)
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                    )
                )
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .forcePathStyle(true)
                .build();
    }

}
