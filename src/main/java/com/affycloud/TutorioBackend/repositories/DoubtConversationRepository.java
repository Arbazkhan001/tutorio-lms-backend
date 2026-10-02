package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.CourseType;
import com.affycloud.TutorioBackend.models.DoubtConversation;
import com.affycloud.TutorioBackend.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoubtConversationRepository extends JpaRepository<DoubtConversation, UUID> {
    Optional<DoubtConversation> findByCourseTypeIdAndStudentId(UUID courseTypeId, UUID studentId);
    @Query("select distinct dc.courseType from DoubtConversation dc")
    List<CourseType> findDistinctCourseTypes();

    @Query("select distinct dc.student from DoubtConversation dc")
    List<Student> findDistinctStudents();

    List<DoubtConversation> findByCourseTypeId(UUID courseTypeId);
    List<DoubtConversation> findByStudentId(UUID studentId);
}
