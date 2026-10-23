package com.microservice.backupserver.infrastructure.adapters;

import com.microservice.backupserver.application.exceptions.StorageOperationException;
import com.microservice.backupserver.application.gateways.FileStorageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class S3FileStorageAdapter implements FileStorageGateway {

    @Value("${storage.s3.bucket-name}")
    private String bucketName;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;


    @Override
    public String generateUploadPresignedUrl(String fileStoragePath, Duration duration) {

        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileStoragePath)
                    .build();

            PutObjectPresignRequest objectPresignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .putObjectRequest(objectRequest)
                    .build();

            return s3Presigner.presignPutObject(objectPresignRequest).url().toString();
        } catch (Exception e) {
            throw new StorageOperationException("Erro ao se comunicar com o Storage: ", e);
        }
    }

    @Override
    public String generateDownloadPresignedUrl(String fileStoragePath, Duration duration) {

        try {
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileStoragePath)
                    .build();

            GetObjectPresignRequest objectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(objectRequest)
                    .build();

            return s3Presigner.presignGetObject(objectPresignRequest).url().toString();
        } catch (Exception e) {
            throw new StorageOperationException("Erro ao se comunicar com o Storage: ", e);
        }
    }

    @Override
    public void deleteFile(String fileStoragePath) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileStoragePath)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new StorageOperationException("Erro ao se comunicar com o Storage: ", e);
        }
    }
}
