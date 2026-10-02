//package com.affycloud.TutorioBackend.controllers;
//
//import com.affycloud.TutorioBackend.models.Ebook;
//import com.affycloud.TutorioBackend.models.Video;
//import com.affycloud.TutorioBackend.repositories.EbookRepository;
//import com.affycloud.TutorioBackend.services.StudentService;
//import com.affycloud.TutorioBackend.services.VideoService;
//import com.affycloud.TutorioBackend.utils.JwtUtil;
//import io.swagger.v3.oas.annotations.Operation;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.Resource;
//import org.springframework.http.*;
//import org.springframework.util.StreamUtils;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.GCMParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//import java.security.SecureRandom;
//import java.util.Base64;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//
//@RestController
//@RequestMapping("/api/proxy")
//public class VideoProxyController {
//
//    @Autowired
//    private VideoService videoService;
//
//    @Autowired
//    private EbookRepository ebookRepository;
//
//
//    @Autowired
//    private StudentService studentService;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//
//    private static final Logger logger = LoggerFactory.getLogger(VideoProxyController.class);
//
//    // 16-byte key (must be exactly 16 bytes for AES-128)
//    private static final String SECRET_KEY = "EffervescentLMS!";
//    private static final String GOOGLE_DRIVE_URL = "https://drive.google.com/uc?id=%s&export=download";
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    // Constants for AES/GCM
//    private static final int GCM_IV_LENGTH = 12; // 12-byte IV for GCM
//    private static final int GCM_TAG_LENGTH = 128; // 128-bit authentication tag
//
//    /**
//     * Endpoint to encrypt the file ID from a video's URL.
//     * Given a video ID, this fetches the video, extracts the Google Drive file ID from its URL,
//     * encrypts it using AES/GCM, and returns a streamId.
//     */
//    @Operation(summary = "Encrypt video file ID")
//    @GetMapping("/encrypt-video/{videoId}")
//    public ResponseEntity<?> encryptVideoFileId(@PathVariable UUID videoId) {
//        Optional<Video> videoOptional = videoService.getVideoById(videoId);
//
//        if (videoOptional.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("message", "Video not found", "error", true));
//        }
//        Video video = videoOptional.get();
//        UUID courseTypeId = video.getChapter().getSubject().getCourseType().getId();
//        String fileId = extractFileId(video.getUrl());
//        logger.info("Extracted fileId: {}", fileId);
//        logger.info("courseTypeId: {}", courseTypeId);
//
//        if (fileId == null) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(Map.of("message", "Invalid video URL. Unable to extract file ID.", "error", true));
//        }
//        try {
//            String encryptedId = encrypt(fileId);
//            return ResponseEntity.ok(Map.of(
//                    "message", "Encrypted successfully",
//                    "error", false,
//                    "streamId", encryptedId,
//                    "courseTypeId", courseTypeId
//            ));
//        } catch (Exception e) {
//            logger.error("Encryption error", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("message", "Encryption failed: " + e.getMessage(), "error", true));
//        }
//    }
//
//    /**
//     * Utility method to extract the file ID from a Google Drive URL.
//     */
//    private String extractFileId(String url) {
//        if (url == null || url.isEmpty()) return null;
//        Pattern pattern = Pattern.compile("https://drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)/view");
//        Matcher matcher = pattern.matcher(url);
//        if (matcher.find()) {
//            return matcher.group(1);
//        }
//        return null;
//    }
//
//    /**
//     * Endpoint to proxy the video stream from Google Drive with Range support.
//     * It decrypts the streamId to get the file ID, then fetches the video from Google Drive.
//     * The client’s Range header is forwarded and the Content-Range header from Google Drive is passed along.
//     */
//    @Operation(summary = "Proxy video stream")
//    @GetMapping("/proxy-video/{streamId}")
//    public ResponseEntity<StreamingResponseBody> proxyVideoWithToken(
//            @PathVariable String streamId,
//            HttpServletRequest request) {
//        try {
//            String fileId = decrypt(streamId);
//            logger.info("Decrypted fileId: {}", fileId);
//            String fileUrl = String.format(GOOGLE_DRIVE_URL, fileId);
//            String rangeHeader = request.getHeader("Range");
//            HttpHeaders headers = new HttpHeaders();
//            headers.set(HttpHeaders.ACCEPT, "video/mp4");
//            if (rangeHeader != null) {
//                headers.set("Range", rangeHeader);
//            }
//            ResponseEntity<Resource> driveResponse = restTemplate.exchange(
//                    fileUrl, HttpMethod.GET, new HttpEntity<>(headers), Resource.class);
//            if (!driveResponse.getStatusCode().is2xxSuccessful() || driveResponse.getBody() == null) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//            }
//            StreamingResponseBody responseBody = outputStream -> {
//                try (InputStream inputStream = driveResponse.getBody().getInputStream()) {
//                    StreamUtils.copy(inputStream, outputStream);
//                }
//            };
//            HttpHeaders responseHeaders = new HttpHeaders();
//            responseHeaders.setContentType(MediaType.parseMediaType("video/mp4"));
//            responseHeaders.set("Accept-Ranges", "bytes");
//            if (driveResponse.getHeaders().containsKey("Content-Range")) {
//                responseHeaders.set("Content-Range", driveResponse.getHeaders().getFirst("Content-Range"));
//            }
//            if (driveResponse.getHeaders().containsKey(HttpHeaders.CONTENT_LENGTH)) {
//                responseHeaders.setContentLength(
//                        Long.parseLong(driveResponse.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH)));
//            }
//            HttpStatus status = (rangeHeader != null) ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
//            return new ResponseEntity<>(responseBody, responseHeaders, status);
//        } catch (Exception e) {
//            logger.error("Error during video proxy", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//
//    @GetMapping("/v3/proxy-video/{streamId}")
//    public void proxyVideoStream(
//            @PathVariable String streamId,
//            HttpServletRequest request,
//            HttpServletResponse response) {
//
//        try {
//            String fileId = decrypt(streamId);
//            String fileUrl = String.format("https://drive.google.com/uc?export=download&id=%s", fileId);
//
//            // Open connection to Google Drive
//            URL url = new URL(fileUrl);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setInstanceFollowRedirects(true); // handle redirects
//
//            // Forward the Range header from the client
//            String range = request.getHeader("Range");
//            if (range != null) {
//                connection.setRequestProperty("Range", range);
//                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
//            } else {
//                response.setStatus(HttpServletResponse.SC_OK);
//            }
//
//            connection.setRequestProperty("Accept", "video/mp4");
//            connection.connect();
//
//            // Set response headers
//            String contentType = connection.getContentType();
//            if (contentType == null || contentType.equals("application/octet-stream")) {
//                response.setContentType("video/mp4");
//            } else {
//                response.setContentType(contentType);
//            }
//
//            String contentRange = connection.getHeaderField("Content-Range");
//            if (contentRange != null) {
//                response.setHeader("Content-Range", contentRange);
//            }
//
//            String contentLength = connection.getHeaderField("Content-Length");
//            if (contentLength != null) {
//                response.setHeader("Content-Length", contentLength);
//            }
//
//            response.setHeader("Accept-Ranges", "bytes");
//            response.setHeader("Content-Disposition", "inline; filename=\"video.mp4\"");
//
//            // Stream the video content to the client
//            try (InputStream in = connection.getInputStream();
//                 OutputStream out = response.getOutputStream()) {
//
//                byte[] buffer = new byte[65536]; // 64 KB buffer
//                int bytesRead;
//                while ((bytesRead = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, bytesRead);
//                }
//                out.flush();
//            }
//
//        } catch (Exception e) {
//            logger.error("Proxy streaming failed", e);
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    /**
//     * Endpoint to encrypt the file ID from an ebook's URL.
//     * Given an ebook ID, this fetches the ebook, extracts the Google Drive file ID from its URL,
//     * encrypts it using AES/GCM, and returns a streamId.
//     */
//    @Operation(summary = "Encrypt ebook file ID")
//    @GetMapping("/encrypt-ebook/{ebookId}")
//    public ResponseEntity<?> encryptEbookFileId(@PathVariable UUID ebookId) {
//        Optional<Ebook> ebookOptional = ebookRepository.findById(ebookId);
//        if (ebookOptional.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("message", "Ebook not found", "error", true));
//        }
//        Ebook ebook = ebookOptional.get();
//        String fileId = extractFileIdOfEbook(ebook.getUrl());
//        logger.info("Extracted fileId: {}", fileId);
//        if (fileId == null) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(Map.of("message", "Invalid ebook URL. Unable to extract file ID.", "error", true));
//        }
//        try {
//            String encryptedId = encrypt(fileId);
//            return ResponseEntity.ok(Map.of(
//                    "message", "Encrypted successfully",
//                    "error", false,
//                    "streamId", encryptedId
//            ));
//        } catch (Exception e) {
//            logger.error("Encryption error", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("message", "Encryption failed: " + e.getMessage(), "error", true));
//        }
//    }
//
//    /**
//     * Endpoint to proxy the ebook stream from Google Drive with Range support.
//     * It decrypts the streamId to get the file ID, then fetches the ebook from Google Drive.
//     * The client’s Range header is forwarded and the Content-Range header from Google Drive is passed along.
//     */
//    @Operation(summary = "Proxy ebook stream")
//    @GetMapping("/proxy-ebook/{streamId}")
//    public ResponseEntity<StreamingResponseBody> proxyEbookWithToken(
//            @PathVariable String streamId,
//            HttpServletRequest request) {
//        try {
//            String fileId = decrypt(streamId);
//            logger.info("Decrypted fileId: {}", fileId);
//            String fileUrl = String.format(GOOGLE_DRIVE_URL, fileId);
//            String rangeHeader = request.getHeader("Range");
//            HttpHeaders headers = new HttpHeaders();
//            headers.set(HttpHeaders.ACCEPT, "application/pdf");
//            if (rangeHeader != null) {
//                headers.set("Range", rangeHeader);
//            }
//            ResponseEntity<Resource> driveResponse = restTemplate.exchange(
//                    fileUrl, HttpMethod.GET, new HttpEntity<>(headers), Resource.class);
//            if (!driveResponse.getStatusCode().is2xxSuccessful() || driveResponse.getBody() == null) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//            }
//            StreamingResponseBody responseBody = outputStream -> {
//                try (InputStream inputStream = driveResponse.getBody().getInputStream()) {
//                    StreamUtils.copy(inputStream, outputStream);
//                }
//            };
//            HttpHeaders responseHeaders = new HttpHeaders();
//            responseHeaders.setContentType(MediaType.APPLICATION_PDF);
//            responseHeaders.set("Accept-Ranges", "bytes");
//            if (driveResponse.getHeaders().containsKey("Content-Range")) {
//                responseHeaders.set("Content-Range", driveResponse.getHeaders().getFirst("Content-Range"));
//            }
//            if (driveResponse.getHeaders().containsKey(HttpHeaders.CONTENT_LENGTH)) {
//                responseHeaders.setContentLength(
//                        Long.parseLong(driveResponse.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH)));
//            }
//            HttpStatus status = (rangeHeader != null) ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
//            return new ResponseEntity<>(responseBody, responseHeaders, status);
//        } catch (Exception e) {
//            logger.error("Error during ebook proxy", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//    private String extractFileIdOfEbook(String url) {
//        if (url == null || url.isEmpty()) return null;
//        Pattern pattern = Pattern.compile("https://drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)/view");
//        Matcher matcher = pattern.matcher(url);
//        if (matcher.find()) {
//            return matcher.group(1);
//        }
//        return null;
//    }
//
//
//    /**
//     * Encrypts the provided data (Google Drive file ID) using AES/GCM/NoPadding.
//     * A random IV is generated and prepended to the ciphertext.
//     */
//    private String encrypt(String data) throws Exception {
//        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
//        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
//
//        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//        byte[] iv = new byte[GCM_IV_LENGTH];
//        SecureRandom secureRandom = new SecureRandom();
//        secureRandom.nextBytes(iv);
//
//        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
//        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
//
//        byte[] ciphertext = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
//
//        // Prepend IV to the ciphertext so it can be used during decryption.
//        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
//        byteBuffer.put(iv);
//        byteBuffer.put(ciphertext);
//        byte[] cipherMessage = byteBuffer.array();
//
//        return Base64.getUrlEncoder().encodeToString(cipherMessage);
//    }
//
//    /**
//     * Decrypts the provided encrypted data using AES/GCM/NoPadding.
//     * The method expects the IV to be prepended to the ciphertext.
//     */
//    private String decrypt(String encryptedData) throws Exception {
//        byte[] cipherMessage = Base64.getUrlDecoder().decode(encryptedData);
//        ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
//
//        byte[] iv = new byte[GCM_IV_LENGTH];
//        byteBuffer.get(iv);
//
//        byte[] ciphertext = new byte[byteBuffer.remaining()];
//        byteBuffer.get(ciphertext);
//
//        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
//        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
//
//        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
//        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
//
//        byte[] decryptedBytes = cipher.doFinal(ciphertext);
//        return new String(decryptedBytes, StandardCharsets.UTF_8);
//    }
//
//
//}