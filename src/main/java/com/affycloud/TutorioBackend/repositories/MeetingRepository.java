package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.CourseAttempt;
import com.affycloud.TutorioBackend.models.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
    Optional<Meeting> findByMeetingId(String meetingId);
    List<Meeting> findByCourseAttemptId(UUID courseAttemptId);
    List<Meeting> findByCourseAttemptIdIn(List<UUID> courseAttemptIds);
    List<Meeting> findByCourseAttempt(CourseAttempt courseAttempt);



    


}
