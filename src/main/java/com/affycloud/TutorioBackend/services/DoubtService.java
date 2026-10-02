package com.affycloud.TutorioBackend.services;


import com.affycloud.TutorioBackend.dtos.course.courseType.CourseTypeDetailDto;
import com.affycloud.TutorioBackend.dtos.doubt.*;
import com.affycloud.TutorioBackend.dtos.reponse.PaginatedResponse;
import com.affycloud.TutorioBackend.enums.Role;

import java.util.List;
import java.util.UUID;

public interface DoubtService {
    DoubtConversationDto createConversation(UUID courseTypeId, UUID studentId,
                                         String initialMessage,
                                         UUID senderId,Role senderRole);
    DoubtMessageDto addMessage(UUID conversationId, UUID senderId,
                               Role senderRole, String messageText);
    DoubtConversationDto getConversation(UUID conversationId);

    PaginatedResponse<DoubtMessageDto> getPaginatedMessagesByStudentAndCourse(UUID studentId, UUID courseTypeId, int page, int size);

    // New API: get distinct course types from doubt conversations
    List<DoubtCourseDetailDto> getAllCourseTypes();

    List<SenderStudentDetailDto> getAllStudents();

    // New API: get all student conversations for a given courseTypeId
    List<DoubtConversationStudentsListDto> getConversationsByCourseTypeId(UUID courseTypeId);
    List<CourseTypeDetailDto> getCourseTypesByStudentId(UUID studentId);
    List<LatestDoubtMessageDto> getAllLatestDoubts();

}
