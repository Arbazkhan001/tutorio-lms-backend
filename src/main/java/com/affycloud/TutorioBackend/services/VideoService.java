package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.dtos.video.VideoRequestDto;
import com.affycloud.TutorioBackend.models.Video;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface VideoService {


    ApiResponseWithMeta saveVideo(VideoRequestDto dto);
    Map<String, Object> getVideosByCourseType(UUID chapterId);
     Optional<Video> getVideoById(UUID id);
    List<Video> getAllVideos();
    ApiResponse deleteVideoById(UUID id);
    void updateVideo(UUID id,VideoRequestDto dto);
}
