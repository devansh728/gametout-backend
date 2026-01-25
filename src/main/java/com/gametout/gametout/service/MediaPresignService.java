package com.gametout.gametout.service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;


@Service
@Slf4j
public class MediaPresignService {

    private final S3Presigner presigner;
    private final String bucket;
    private final S3Client s3Client;
    private final String publicEndpoint;

    public MediaPresignService(
            S3Presigner presigner,
            S3Client s3Client,
            @Value("${storage.bucket}") String bucket,
            @Value("${storage.public-endpoint}") String publicEndpoint

    ) {
        this.presigner = presigner;
        this.bucket = bucket;
        this.s3Client = s3Client;
        this.publicEndpoint = publicEndpoint;
    }

    @PostConstruct
    public void init() {
        try {
            boolean bucketExists = s3Client.listBuckets().buckets().stream()
                    .anyMatch(b -> b.name().equals(bucket));

            if (!bucketExists) {
                log.info("Creating bucket: {}", bucket);
                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(bucket)
                        .build());
            }
            setPublicAccessPolicy();

        } catch (S3Exception e) {
            log.error("Failed to initialize bucket storage: ", e);
        }
    }

    private void setPublicAccessPolicy() {
        try {
            String bucketPolicy = String.format("""
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": "*",
                            "Action": [
                                "s3:GetObject"
                            ],
                            "Resource": [
                                "arn:aws:s3:::%s/*"
                            ]
                        }
                    ]
                }
                """, bucket);

            s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                    .bucket(bucket)
                    .policy(bucketPolicy)
                    .build());
            
            log.info("Public access policy set for bucket: {}", bucket);

        } catch (S3Exception e) {
            log.error("Could not set bucket policy. Check permissions.", e);
        }
    }

    public Map<String, String> presignUpload(
            String filename,
            String contentType
    ) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String key = String.format("uploads/%s/%s-%s",
                datePath,
                UUID.randomUUID(),
                filename);
        

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(objectRequest)
                        .build();

        String uploadUrl = presigner
                .presignPutObject(presignRequest)
                .url()
                .toString();

        return Map.of(
                "uploadUrl", uploadUrl,
                "objectKey", key,
                "publicUrl", getPublicUrl(key)
        );
    }

    public String getPublicUrl(String objectKey) {
        String cleanEndpoint = publicEndpoint.endsWith("/") ? publicEndpoint.substring(0, publicEndpoint.length() - 1) : publicEndpoint;
        return String.format("%s/%s/%s", cleanEndpoint, bucket, objectKey);
    }
}


