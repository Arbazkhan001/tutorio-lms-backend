//package com.affycloud.TutorioBackend.controllers;
//
//
//import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
//import com.affycloud.TutorioBackend.models.Video;
//import com.affycloud.TutorioBackend.repositories.VideoRepository;
//import com.affycloud.TutorioBackend.services.StudentService;
//import com.affycloud.TutorioBackend.services.VideoService;
//import com.affycloud.TutorioBackend.services.implementation.drive.GoogleDriveService;
//import com.affycloud.TutorioBackend.utils.JwtUtil;
//import com.google.api.client.http.HttpHeaders;
//import com.google.api.client.http.HttpResponse;
//import com.google.api.services.drive.Drive;
//import com.google.api.services.drive.model.File;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.security.GeneralSecurityException;
//import java.util.UUID;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@RestController
//@RequestMapping("/api/streaming")
//public class VideoStreamingController {
//
//    @Autowired
//    private VideoService videoService;
//
//    @Autowired
//    private VideoRepository videoRepository;
//
//    @Autowired
//    private StudentService studentService;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @Value("${jwt.secret}")
//    private String secret;
//
//    @Autowired
//    private GoogleDriveService googleDriveService;
//
//
//    @GetMapping("/stream")
//    public void streamVideo(
//            @RequestParam UUID id,
//            @RequestHeader("Range") String range,
//            HttpServletResponse response
//    ) throws IOException, GeneralSecurityException {
//        // 1. Fetch video entity from DB
//        Video video = videoRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
//
//        // 2. Extract Google Drive file ID from the URL
//        String fileId = extractDriveFileId(video.getUrl());
//
//
//        Drive driveService = googleDriveService.getDriveService();
//
//        // Get file metadata
//        File file = driveService.files().get(fileId).setFields("size,mimeType").execute();
//        long fileSize = Long.parseLong(file.getSize().toString());
//        String mimeType = file.getMimeType();
//
//        // Parse Range header
//        Matcher matcher = Pattern.compile("bytes=(\\d+)-(\\d*)").matcher(range);
//        if (!matcher.matches()) {
//            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Range Header");
//            return;
//        }
//
//        long start = Long.parseLong(matcher.group(1));
//        long end = matcher.group(2).isEmpty() ? Math.min(start + 1024 * 1024, fileSize - 1) : Long.parseLong(matcher.group(2));
//
//        if (matcher.group(2).isEmpty()) {
//            // Default to a 1MB chunk or until end of file
//            end = Math.min(start + 1024 * 1024 - 1, fileSize - 1);
//        } else {
//            end = Long.parseLong(matcher.group(2));
//            if (end >= fileSize) end = fileSize - 1;
//        }
//
//        if (start > end || start >= fileSize) {
//            response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, "Invalid byte range");
//            return;
//        }
//        long contentLength = end - start + 1;
//
//        // Request partial content
//        HttpResponse driveResponse = driveService
//                .files()
//                .get(fileId)
//                .setAlt("media")
//                .buildHttpRequest()
//                .setHeaders(new HttpHeaders().setRange("bytes=" + start + "-" + end))
//                .execute();
//
//        // Set response headers
//        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
//        response.setHeader("Content-Type", mimeType);
//        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
//        response.setHeader("Accept-Ranges", "bytes");
//        response.setHeader("Content-Length", String.valueOf(contentLength));
//
//        // Stream data
//        try (InputStream inputStream = driveResponse.getContent();
//             OutputStream outputStream = response.getOutputStream()) {
//            inputStream.transferTo(outputStream);
//        }
//    }
//
//    private String extractDriveFileId(String url) {
//        // Match either URL or "Uploaded Video ID: <id>, Link: ..."
//        Pattern pattern = Pattern.compile(".*?/d/([a-zA-Z0-9_-]+).*|Uploaded Video ID: ([a-zA-Z0-9_-]+),.*");
//        Matcher matcher = pattern.matcher(url);
//        if (matcher.find()) {
//            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
//        }
//        throw new IllegalArgumentException("Invalid Google Drive URL format in video entity");
//    }
//
//}