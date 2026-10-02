package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.Video;

import java.util.UUID;

public interface VideoProgressService {
    void saveProgress(Student student, Video video, double lastWatchedTime, Double watchTime);
    ApiResponseWithMeta getProgress(UUID studentId, UUID videoId);
    ApiResponseWithMeta getLastPlayedVideos(UUID studentId);
    ApiResponseWithMeta getAllProgressByVideoId(UUID videoId);
}
