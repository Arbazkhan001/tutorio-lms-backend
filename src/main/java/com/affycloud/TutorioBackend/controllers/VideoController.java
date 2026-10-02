package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.dtos.video.VideoRequestDto;
import com.affycloud.TutorioBackend.models.Video;
import com.affycloud.TutorioBackend.services.StudentService;
import com.affycloud.TutorioBackend.services.VideoService;
import com.affycloud.TutorioBackend.services.implementation.R2VideoService;
import com.affycloud.TutorioBackend.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private R2VideoService r2VideoService;

    @Autowired
    private JwtUtil jwtUtil;



    @GetMapping("/presigned-upload")
    public ResponseEntity<ApiResponseWithMeta> getPresignedUploadUrl(
            @RequestParam String fileName,
            @RequestParam String contentType
    ) {
        Map<String, String> urls =
                r2VideoService.generatePresignedUploadUrl(fileName, contentType);

        Map<String, Object> meta = new HashMap<>();
        meta.put("uploadUrl", urls.get("uploadUrl"));
        meta.put("fileUrl", urls.get("fileUrl"));

        return ResponseEntity.ok(
                new ApiResponseWithMeta(
                        "Presigned upload URL generated successfully",
                        false,
                        meta
                )
        );
    }


    /**
     * STEP 2: Save video metadata after upload
     */
    @PostMapping
    public ResponseEntity<ApiResponseWithMeta> saveVideo(
            @RequestBody VideoRequestDto dto
    ) {
        return ResponseEntity.ok(videoService.saveVideo(dto));
    }



    @Operation(summary = "Get videos by course type", description = "Fetches all videos associated with a given course type ID.")
    @GetMapping("/by-course-type/{courseTypeId}")
    public ResponseEntity<Map<String, Object>> getVideosByCourseType(@PathVariable UUID courseTypeId) {
        Map<String, Object> response = videoService.getVideosByCourseType(courseTypeId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Get a video by ID", description = "Retrieves a video based on its unique ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWithMeta> getVideoById(@PathVariable UUID id) {
        return videoService.getVideoById(id)
                .map(video -> {
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("video", video);
                    return ResponseEntity.ok(new ApiResponseWithMeta("Video found", false, meta));
                })
                .orElse(ResponseEntity.status(404)
                        .body(new ApiResponseWithMeta("Video not found", true, null)));
    }




    @Operation(summary = "Get all videos", description = "Retrieves all videos available in the system.")
    @GetMapping
    public ResponseEntity<ApiResponseWithMeta> getAllVideos() {
        List<Video> videos = videoService.getAllVideos();

        Map<String, Object> meta = new HashMap<>();
        meta.put("totalVideos", videos.size());
        meta.put("videos", videos);

        ApiResponseWithMeta response = new ApiResponseWithMeta("Videos fetched successfully", false, meta);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a video")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteVideo(@PathVariable UUID id) {
        return ResponseEntity.ok(videoService.deleteVideoById(id));
    }

    @Operation(summary = "Update Video name")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateVideo(@PathVariable UUID id, @RequestBody VideoRequestDto dto) {

        videoService.updateVideo(id,dto);

        ApiResponse response = new ApiResponse(
                "Video updated successfully",
                false
        );

        return ResponseEntity.ok(response);
    }
}


/*




@Operation(summary = "Upload a video", description = "Uploads a video file along with title, description, and optional course type ID.")
@PostMapping("/upload")
public ResponseEntity<Map<String, Object>> uploadVideo(
        @RequestParam("file") MultipartFile file,
        @RequestParam("title") String title,
        @RequestParam("description") String description,
        @RequestParam("countLimit") Double countLimit,
        @RequestParam(value = "chapterId", required = false) UUID chapterId) {

    Map<String, Object> response = videoService.uploadVideo(file, title, description, countLimit, chapterId);

    if (response.get("response").equals("Video uploaded successfully")) {
        return ResponseEntity.status(HttpStatus.OK).body(response);
    } else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}


@Operation(summary = "Upload videos from folder", description = "Uploads multiple videos from a Google Drive folder using folder ID.")
@PostMapping("/upload-from-folder")
public ResponseEntity<Map<String, Object>> uploadVideosFromFolder(
        @RequestParam("folderId") String folderId,
        @RequestParam("chapterId") UUID chapterId,
        @RequestParam("countLimit") Double countLimit) {

    Map<String, Object> response = videoService.uploadVideosFromFolder(folderId, chapterId,countLimit);

    if (response.get("response").equals("Videos uploaded successfully")) {
        return ResponseEntity.status(HttpStatus.OK).body(response);
    } else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}



private static final String SECRET_KEY = "EffervescentLMS2"; // 16-byte AES Key
private static final String GOOGLE_DRIVE_URL = "https://drive.google.com/uc?id=%s&export=download";
private final RestTemplate restTemplate = new RestTemplate();


@Operation(summary = "Encrypt video file ID", description = "Encrypts the file ID of a video for secure streaming.")
@GetMapping("/encrypt-video/{videoId}")
public ResponseEntity<?> encryptVideoFileId(@PathVariable UUID videoId) {
    Optional<Video> videoOptional = videoService.getVideoById(videoId);
    if (videoOptional.isEmpty()) {
        return ResponseEntity.status(404).body(Map.of(
                "message", "Video not found",
                "error", true
        ));
    }
    Video video = videoOptional.get();
    UUID courseTypeId = video.getChapter().getSubject().getCourseType().getId();
    String fileId = extractFileId(video.getUrl());
    if (fileId == null) {
        return ResponseEntity.status(400).body(Map.of(
                "message", "Invalid video URL. Unable to extract file ID.",
                "error", true
        ));
    }
    try {
        String encryptedId = encrypt(fileId);
        return ResponseEntity.ok(Map.of(
                "message", "Encrypted successfully",
                "error", false,
                "streamId", encryptedId,
                "courseTypeId", courseTypeId
        ));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of(
                "message", "Encryption failed: " + e.getMessage(),
                "error", true
        ));
    }
}

private String extractFileId(String url) {
    if (url == null || url.isEmpty()) return null;
    // Regular expression to extract Google Drive file ID
    Pattern pattern = Pattern.compile("https://drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)/view");
    Matcher matcher = pattern.matcher(url);
    System.out.println("abc" + matcher);
    if (matcher.find()) {
        return matcher.group(1);
    }
    return null;
}

@Operation(summary = "Proxy video with token", description = "Proxies a video stream using an encrypted file ID and JWT token.")
@GetMapping("/v2/proxy-video/{streamId}")
public ResponseEntity<?> proxyVideoWithToken(
        @PathVariable String streamId,
        @RequestHeader("Authorization") String token) {
    try {
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // Validate and extract student details from token
        if (jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired.");
        }
        // Decrypt stream ID
        String fileId = decrypt(streamId);
        String fileUrl = String.format(GOOGLE_DRIVE_URL, fileId);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, "video/mp4");
        ResponseEntity<byte[]> response = restTemplate.exchange(
                fileUrl, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"video.mp4\"")
                    .body(response.getBody());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to fetch video from Google Drive");
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid or expired token: " + e.getMessage());
    }
}

private String encrypt(String data) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
    cipher.init(Cipher.ENCRYPT_MODE, keySpec);
    byte[] encrypted = cipher.doFinal(data.getBytes());
    return Base64.getUrlEncoder().encodeToString(encrypted);
}

private String decrypt(String encryptedData) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
    cipher.init(Cipher.DECRYPT_MODE, keySpec);
    byte[] decodedBytes = Base64.getUrlDecoder().decode(encryptedData);
    return new String(cipher.doFinal(decodedBytes));
}

@Operation(summary = "Encrypt file ID", description = "Encrypts a file ID for secure storage.")
@GetMapping("/v1/encrypt/{fileId}")
public ResponseEntity<String> encryptFileId(@PathVariable String fileId) {
    try {
        String encrypted = encrypt(fileId);
        return ResponseEntity.ok(encrypted);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Encryption failed: " + e.getMessage());
    }
}

@Operation(summary = "Proxy video", description = "Proxies a video stream using an encrypted file ID.")
@GetMapping("/v1/proxy-video/{streamId}")
public ResponseEntity<?> proxyVideo(@PathVariable String streamId) {
    try {
        String fileId = decrypt(streamId);
        String fileUrl = String.format(GOOGLE_DRIVE_URL, fileId);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, "video/mp4");
        ResponseEntity<byte[]> response = restTemplate.exchange(
                fileUrl, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"video.mp4\"")
                    .body(response.getBody());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to fetch video from Google Drive");
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid or expired encrypted ID: " + e.getMessage());
    }
}*/
