package com.playko.zoologico.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.util.Collections;
import java.util.Map;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${s3.bucket-name}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String subirArchivo(byte[] contenido, String key) {
        return subirArchivo(contenido, key,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                Collections.emptyMap());
    }

    public String subirArchivo(byte[] contenido, String key, String contentType) {
        return subirArchivo(contenido, key, contentType, Collections.emptyMap());
    }

    // Nueva: acepta metadata (por ejemplo: creator-email)
    public String subirArchivo(byte[] contenido, String key, String contentType, Map<String, String> metadata) {
        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType);

        if (metadata != null && !metadata.isEmpty()) {
            builder = builder.metadata(metadata);
        }

        PutObjectRequest putObjectRequest = builder.build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(contenido));
        return key;
    }
}