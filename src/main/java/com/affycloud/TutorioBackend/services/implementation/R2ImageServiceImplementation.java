package com.affycloud.TutorioBackend.services.implementation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class R2ImageServiceImplementation {

    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.public-base-url}")
    private String publicBaseUrl;

    public R2ImageServiceImplementation(S3Client s3Client, S3Presigner presigner) {
        this.s3Client = s3Client;
        this.presigner = presigner;
    }

    /**
     * Upload image and return public URL
     */
    public String uploadImage(MultipartFile file) throws IOException {

        validateImage(file);

        String extension = getFileExtension(file.getOriginalFilename());
        String objectKey = UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .cacheControl("public, max-age=31536000, immutable")
                            .build(),
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );
        }

        return publicBaseUrl + "/" + objectKey;
    }



    /**
     * Generate presigned URL for direct client-side upload
     */
    public Map<String, String> generatePresignedUploadUrl(
            String folder,
            String originalFileName,
            String contentType) {

        String extension = getFileExtension(originalFileName);
        String objectKey = folder + "/" + UUID.randomUUID() + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
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

    /**
     * Delete image using object key
     */
    public void deleteImageByKey(String objectKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build());
    }

    public ResponseInputStream<GetObjectResponse> streamFileByUrl(String fileUrl) {
        String objectKey = extractKeyFromUrl(fileUrl);
        return s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build()
        );
    }

    // ---------------- PRIVATE HELPERS ----------------

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty");
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        long maxSize = 20 * 1024 * 1024; // 20 MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Image size exceeds 20MB limit");
        }
    }

    public String uploadFile(String folder, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String objectKey = folder + "/" + UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .cacheControl("public, max-age=31536000")
                            .build(),
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );
        }

        return publicBaseUrl + "/" + objectKey;
    }

    private static final long MAX_EBOOK_SIZE = 100 * 1024 * 1024; // 100 MB
    private static final long MAX_THUMBNAIL_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_EBOOK_TYPES = Set.of(
            "application/pdf",
            "application/epub+zip"
    );
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    public void validateEbookFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Ebook file is required");
        }
        if (file.getSize() > MAX_EBOOK_SIZE) {
            throw new IllegalArgumentException("Ebook file size exceeds 100MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_EBOOK_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only PDF and EPUB files are allowed for ebooks");
        }
    }

    public void validateThumbnail(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Thumbnail file is required");
        }
        if (file.getSize() > MAX_THUMBNAIL_SIZE) {
            throw new IllegalArgumentException("Thumbnail size exceeds 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPEG, PNG, and WebP images are allowed for thumbnails");
        }
    }


    public void deleteFileByUrl(String fileUrl) {

        if (fileUrl == null || fileUrl.isBlank()) return;

        String objectKey = extractKeyFromUrl(fileUrl);

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build()
        );
    }

    private String extractKeyFromUrl(String url) {
        return url.substring(url.indexOf(".in/") + 4);
    }


    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
