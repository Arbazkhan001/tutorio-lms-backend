package com.affycloud.TutorioBackend.services.implementation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class R2VideoService {

    private final S3Presigner presigner;
    private final S3Client s3Client;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.public-base-url}")
    private String publicBaseUrl;

    public R2VideoService(S3Presigner presigner,S3Client s3Client) {
        this.presigner = presigner;
        this.s3Client = s3Client;
    }

    /**
     * Generate presigned URL for direct video upload
     */

    public Map<String, String> generatePresignedUploadUrl(
            String originalFileName,
            String contentType
    ) {

        String extension = getFileExtension(originalFileName);
        String objectKey = "videos/" + UUID.randomUUID() + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(putObjectRequest)
                        .build();

        String uploadUrl = presigner
                .presignPutObject(presignRequest)
                .url()
                .toString();

        String fileUrl = publicBaseUrl + "/" + objectKey;

        Map<String, String> response = new HashMap<>();
        response.put("uploadUrl", uploadUrl);
        response.put("fileUrl", fileUrl);

        return response;
    }


    public void deleteVideo(String fileUrl) {

        if (fileUrl == null || fileUrl.isEmpty()) return;

        // extract object key from full URL
        String objectKey = fileUrl.replace(publicBaseUrl + "/", "");

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        s3Client.deleteObject(deleteRequest);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }


}
