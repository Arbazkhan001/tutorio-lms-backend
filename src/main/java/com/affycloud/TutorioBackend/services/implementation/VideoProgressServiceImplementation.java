package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.dtos.video.VideoDetailDto;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.Video;
import com.affycloud.TutorioBackend.models.VideoProgress;
import com.affycloud.TutorioBackend.repositories.StudentRepository;
import com.affycloud.TutorioBackend.repositories.VideoProgressRepository;
import com.affycloud.TutorioBackend.repositories.VideoRepository;
import com.affycloud.TutorioBackend.services.VideoProgressService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoProgressServiceImplementation implements VideoProgressService {
    @Autowired
    private VideoProgressRepository videoProgressRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void saveProgress(Student student, Video video, double lastWatchedTime, Double watchTime) {
        Optional<VideoProgress> existingProgress = videoProgressRepository.findFirstByStudentAndVideo(student, video);

        if (existingProgress.isPresent()) {
            VideoProgress progress = existingProgress.get();
            progress.setLastWatchedTime(lastWatchedTime);
            progress.setWatchTime(watchTime);
            videoProgressRepository.save(progress);
        } else {
            VideoProgress progress = new VideoProgress();
            progress.setStudent(student);
            progress.setVideo(video);
            progress.setLastWatchedTime(lastWatchedTime);
            progress.setWatchTime(watchTime);
            videoProgressRepository.save(progress);
        }
    }

    @Override
    public ApiResponseWithMeta getProgress(UUID studentId, UUID videoId) {

        Student student = studentRepository.findById(studentId).orElse(null);
        Video video = videoRepository.findById(videoId).orElse(null);

        if (student == null || video == null) {
            return new ApiResponseWithMeta("Student or Video not found", true, null);
        }

        UUID courseTypeId = video.getChapter().getSubject().getCourseType().getId();
        // Fetch the video progress and extract the last watched time, or return 0.0 if none exists.
        Optional<VideoProgress> progress = videoProgressRepository.findFirstByStudentAndVideo(student, video);
        Double lastWatchedTime = progress.map(VideoProgress::getLastWatchedTime).orElse(0.0);
        Double watchTime = progress.map(VideoProgress::getWatchTime).orElse(0.0);

        // Return the progress wrapped in your response format.
        return new ApiResponseWithMeta(
                "Progress retrieved successfully",
                false,
                Map.of("progress", lastWatchedTime,"countLimit", video.getCountLimit(),"paymentType", video.getChapter().getSubject().getCourseType().getCourse().getPaymentType(),"watchTime", watchTime,"courseTypeId",courseTypeId,"videoTitle",video.getTitle() )
        );
    }


    @Override
    public ApiResponseWithMeta getLastPlayedVideos(UUID studentId) {
        Pageable pageable = PageRequest.of(0, 5); // Limit to 5 videos
        List<VideoProgress> progressList = videoProgressRepository.findLatestVideosPerChapter(studentId, pageable);

        List<VideoDetailDto> videoDetails = progressList.stream()
                .map(vp -> modelMapper.map(vp.getVideo(), VideoDetailDto.class))
                .collect(Collectors.toList());

        Map<String, Object> meta = new HashMap<>();
        meta.put("totalVideos", videoDetails.size());
        meta.put("videos", videoDetails);

        return new ApiResponseWithMeta("Last played videos fetched successfully", false, meta);
    }

    @Override
    public ApiResponseWithMeta getAllProgressByVideoId(UUID videoId) {
        Optional<Video> video = videoRepository.findById(videoId);
        if (video.isEmpty()) {
            return new ApiResponseWithMeta("Video not found", true, null);
        }

        List<VideoProgress> progressList = videoProgressRepository.findByVideoId(videoId);

        List<Map<String, Object>> progressData = progressList.stream()
                .map(vp -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", vp.getId());
                    data.put("studentId", vp.getStudent().getId());
                    data.put("studentName", vp.getStudent().getName());
                    data.put("lastWatchedTime", vp.getLastWatchedTime());
                    data.put("watchTime", vp.getWatchTime());
                    data.put("updatedAt", vp.getUpdatedAt());
                    return data;
                })
                .collect(Collectors.toList());

        Map<String, Object> meta = new HashMap<>();
        meta.put("totalRecords", progressData.size());
        meta.put("progresses", progressData);

        return new ApiResponseWithMeta("All progress fetched successfully", false, meta);
    }
}
