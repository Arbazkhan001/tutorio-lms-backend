package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.dtos.video.VideoRequestDto;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.Chapter;
import com.affycloud.TutorioBackend.models.Video;
import com.affycloud.TutorioBackend.repositories.ChapterRepository;
import com.affycloud.TutorioBackend.repositories.VideoRepository;
import com.affycloud.TutorioBackend.services.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoServiceImplementation implements VideoService {

    

    @Autowired
    private VideoRepository videoRepository;


    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private R2VideoService r2VideoService;




    public ApiResponseWithMeta saveVideo(VideoRequestDto dto) {

        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        Video video = new Video();
        video.setTitle(dto.getTitle());
        video.setDescription(dto.getDescription());
        video.setCountLimit(dto.getCountLimit());
        video.setUrl(dto.getVideoUrl());
        video.setChapter(chapter);

        videoRepository.save(video);

        Map<String, Object> meta = new HashMap<>();
        meta.put("video", video);

        return new ApiResponseWithMeta(
                "Video saved successfully",
                false,
                meta
        );
    }




    public Map<String, Object> getVideosByCourseType(UUID chapterId) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> meta = new ArrayList<>();

        try {
            List<Video> videos = videoRepository.findByChapterId(chapterId, Sort.by("createdAt").ascending());

            if (videos.isEmpty()) {
                response.put("meta", meta);  // Empty list when no videos found
                response.put("message", "Videos retrieved successfully");
                response.put("error", false);
            } else {
                response.put("meta", videos);
                response.put("message", "Videos fetched successfully");
                response.put("error", false);
            }
        } catch (Exception e) {
            response.put("meta", meta);  // Empty list in case of error
            response.put("message", "Error fetching videos");
            response.put("error", true);
            response.put("errorDetails", e.getMessage());
        }

        return response;
    }

    public ApiResponse deleteVideoById(UUID id) {

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        // 🔥 delete video from R2 first
        r2VideoService.deleteVideo(video.getUrl());

        // 🗑️ delete DB record
        videoRepository.delete(video);

        return new ApiResponse("Video deleted successfully", false);


    }

    @Transactional
    public void updateVideo(UUID id, VideoRequestDto dto) {

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found"));

        if(dto.getTitle() != null) video.setTitle(dto.getTitle());
        if(dto.getDescription() != null) video.setDescription(dto.getDescription());
        if(dto.getCountLimit() != null) video.setCountLimit(dto.getCountLimit());
    }

    public Optional<Video> getVideoById(UUID id) {
        return videoRepository.findById(id);
    }

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

}




    /*public boolean deleteVideoById(UUID id) {
        if (videoRepository.existsById(id)) {
            videoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Map<String, Object> updateVideo(UUID videoId, String title, String description, Double countLimit, MultipartFile file) throws IOException {
        Map<String, Object> response = new HashMap<>();

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found with ID: " + videoId));

        if (title != null) {
            video.setTitle(title);
        }
        if (description != null) {
            video.setDescription(description);
        }
        if (countLimit != null) {
            video.setCountLimit(countLimit);
        }

        if (file != null && !file.isEmpty()) {
            try {
                String videoUrl = googleDriveVideoService.uploadVideo(file);
                video.setUrl(videoUrl);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException("Error uploading video: " + e.getMessage());
            }
        }

        videoRepository.save(video);

        response.put("response", "Video updated successfully");
        response.put("video", video);
        return response;
    }
*/
/*


public Map<String, Object> uploadVideo(MultipartFile file, String title, String description,Double countLimit, UUID chapterId) {
    Map<String, Object> response = new HashMap<>();

    try {
        String videoUrl = googleDriveVideoService.uploadVideo(file);

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found with ID: " + chapterId));

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setCountLimit(countLimit);
        video.setUrl(videoUrl);
        video.setChapter(chapter);

        videoRepository.save(video);

        response.put("response", "Video uploaded successfully");
        response.put("video", video);
        return response;
    } catch (IOException | GeneralSecurityException e) {
        response.put("response", "Error uploading video");
        response.put("message", e.getMessage());
        return response;
    }
}

public List<Video> getVideosByChapter(UUID chapterId) {
    return videoRepository.findByChapterId(chapterId);
}


public Map<String, Object> uploadVideosFromFolder(String folderId, UUID chapterId,Double countLimit) {
    Map<String, Object> response = new HashMap<>();

    try {
        List<File> videoFiles = googleDriveVideoService.getVideosFromFolder(folderId);

        List<String> existingVideoUrls = videoRepository.findByChapterId(chapterId)
                .stream()
                .map(Video::getUrl)
                .collect(Collectors.toList());

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found with ID: " + chapterId));

        List<Video> newVideos = new ArrayList<>();
        int newUploadCount = 0;

        for (File videoFile : videoFiles) {
            String videoUrl = videoFile.getWebViewLink();

            if (!existingVideoUrls.contains(videoUrl)) {
                Video video = new Video();
                video.setTitle(videoFile.getName());
                video.setDescription("Video from Google Drive folder");
                video.setCountLimit(countLimit);
                video.setUrl(videoUrl);
                */
/*video.setCourseTypeId(courseTypeId);*//*

                video.setChapter(chapter);

                newVideos.add(video);
                newUploadCount++;
            }
        }

        if (!newVideos.isEmpty()) {
            videoRepository.saveAll(newVideos);
            response.put("response", "Videos uploaded successfully");
            response.put("newVideosUploaded", newUploadCount);
            return response;
        } else {
            response.put("response", "Already up to date");
            return response;
        }
    } catch (IOException | GeneralSecurityException e) {
        response.put("response", "Error uploading videos from Google Drive folder");
        response.put("message", e.getMessage());
        return response;
    }
}
*/
