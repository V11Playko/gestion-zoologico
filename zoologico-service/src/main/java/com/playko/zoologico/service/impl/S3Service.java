package com.playko.zoologico.service.impl;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName = "excel-nlb";

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String subirArchivo(byte[] contenido, String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(contenido));

        return key;
    }
}
