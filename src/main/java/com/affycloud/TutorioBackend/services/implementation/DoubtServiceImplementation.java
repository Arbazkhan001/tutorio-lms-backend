package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.course.courseType.CourseTypeDetailDto;
import com.affycloud.TutorioBackend.dtos.doubt.*;
import com.affycloud.TutorioBackend.dtos.reponse.PaginatedResponse;
import com.affycloud.TutorioBackend.enums.NotificationType;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.DoubtService;
import com.affycloud.TutorioBackend.services.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DoubtServiceImplementation implements DoubtService {

    @Autowired
    private DoubtConversationRepository conversationRepository;

    @Autowired
    private DoubtMessageRepository messageRepository;

    @Autowired
    private CourseTypeRepository courseTypeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;  // For teacher/admin lookup

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // For WebSocket messaging

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public DoubtConversationDto createConversation(UUID courseTypeId, UUID studentId,
                                                   String initialMessage,
                                                   UUID senderId,Role senderRole) {
        // Fetch CourseType and Student.
        CourseType courseType = courseTypeRepository.findById(courseTypeId)
                .orElseThrow(() -> new RuntimeException("CourseType not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Find or create a conversation.
        DoubtConversation conversation = conversationRepository
                .findByCourseTypeIdAndStudentId(courseTypeId, studentId)
                .orElseGet(() -> {
                    DoubtConversation conv = new DoubtConversation();
                    conv.setCourseType(courseType);
                    conv.setStudent(student);
                    return conversationRepository.save(conv);
                });

        // **Use the existing addMessage logic to add the first message**
        addMessage(conversation.getId(), senderId, senderRole, initialMessage);

        // Reload conversation to include the new message.
        conversation = conversationRepository.findById(conversation.getId())
                .orElseThrow(() -> new RuntimeException("Conversation not found after creation"));

        return convertToDto(conversation);
    }


    @Override
    public DoubtMessageDto addMessage(UUID conversationId, UUID senderId,
                                      Role senderRole, String messageText) {
        DoubtConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        DoubtMessage message = new DoubtMessage();
        message.setConversation(conversation);
        message.setSenderRole(senderRole);
        message.setMessageText(messageText);
        if (senderRole == Role.STUDENT) {
            Student senderStudent = studentRepository.findById(senderId)
                    .orElseThrow(() -> new RuntimeException("Sender (student) not found"));
            message.setStudentSender(senderStudent);
            message.setUserSender(null);

            // Only if student sends a message, send notification to admin
            notificationService.createNotification(
                    conversation.getId(),
                    NotificationType.DOUBT,
                    null,
                    Role.ADMIN, // ✅ Correct: ADMIN ko bhejna hai
                    "New doubt from " + senderStudent.getName(),
                    "{ studentId: " + senderId + " +  courseTypeId : " + conversation.getCourseType().getId() + " }"
            );
        } else {
            User senderUser = userRepository.findById(senderId)
                    .orElseThrow(() -> new RuntimeException("Sender (user) not found"));
            message.setUserSender(senderUser);
            message.setStudentSender(null);

            // Only if ADMIN sends a message, send notification to STUDENT
            notificationService.createNotification(
                    conversation.getId(),
                    NotificationType.DOUBT,
                    conversation.getStudent(),
                    Role.STUDENT, // ✅ Correct: STUDENT ko bhejna hai
                    "Doubt Answer from " + senderUser.getName(),
                    "{ studentId: " + senderId + " +  courseTypeId : " + conversation.getCourseType().getId() + " }"
            );
        }
        DoubtMessage savedMessage = messageRepository.save(message);
        DoubtMessageDto messageDto = modelMapper.map(savedMessage, DoubtMessageDto.class);

        // Publish the new message using the topic based on courseTypeId and studentId
        UUID courseTypeId = conversation.getCourseType().getId();
        UUID studentId = conversation.getStudent().getId();
        String destination = "/topic/conversations/course/" + courseTypeId + "/student/" + studentId;
        messagingTemplate.convertAndSend(destination, messageDto);

        return messageDto;
    }

    @Override
    public DoubtConversationDto getConversation(UUID conversationId) {
        DoubtConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return convertToDto(conversation);
    }

    public PaginatedResponse<DoubtMessageDto> getPaginatedMessagesByStudentAndCourse(UUID studentId, UUID courseTypeId, int page, int size) {
        DoubtConversation conversation = conversationRepository
                .findByCourseTypeIdAndStudentId(courseTypeId, studentId)
                .orElseThrow(() -> new RuntimeException("Conversation not found for given student and course type"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DoubtMessage> messagePage = messageRepository.findByConversationId(conversation.getId(), pageable);

        List<DoubtMessageDto> messageDtos = messagePage.getContent().stream()
                .map(msg -> modelMapper.map(msg, DoubtMessageDto.class))
                .collect(Collectors.toList());

        return PaginatedResponse.<DoubtMessageDto>builder()
                .totalPages(messagePage.getTotalPages())
                .totalItems(messagePage.getTotalElements())
                .size(size)
                .currentPage(page)
                .meta(messageDtos)
                .build();
    }

    @Override
    public List<DoubtCourseDetailDto> getAllCourseTypes() {
        List<CourseType> courseTypes = conversationRepository.findDistinctCourseTypes();
        return courseTypes.stream()
                .map(this::convertToCourseDetailDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SenderStudentDetailDto> getAllStudents() {
        List<Student> students = conversationRepository.findDistinctStudents();
        return students.stream()
                .map(student -> modelMapper.map(student, SenderStudentDetailDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<DoubtConversationStudentsListDto> getConversationsByCourseTypeId(UUID courseTypeId) {
        List<DoubtConversation> conversations = conversationRepository.findByCourseTypeId(courseTypeId);
        return conversations.stream()
                .map(this::convertToStudentsListDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseTypeDetailDto> getCourseTypesByStudentId(UUID studentId) {
        List<DoubtConversation> conversations = conversationRepository.findByStudentId(studentId);
        return conversations.stream()
                .map(conversation -> modelMapper.map(conversation, CourseTypeDetailDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<LatestDoubtMessageDto> getAllLatestDoubts() {
        // Retrieve all doubt conversations
        List<DoubtConversation> conversations = conversationRepository.findAll();

        // For each conversation, pick the latest message (if present) and map to DTO
        return conversations.stream().map(conversation -> {
            LatestDoubtMessageDto dto = new LatestDoubtMessageDto();
            dto.setConversationId(conversation.getId());
            dto.setStudent(modelMapper.map(conversation.getStudent(), SenderStudentDetailDto.class));
            dto.setCourseType(convertToCourseDetailDto(conversation.getCourseType()));

            dto.setConversationCreatedAt(conversation.getCreatedAt());
            dto.setConversationUpdatedAt(conversation.getUpdatedAt());

            // Find the latest message (newest first) in this conversation.
            Optional<DoubtMessage> latestMsgOpt = conversation.getMessages().stream()
                    .sorted(Comparator.comparing(DoubtMessage::getCreatedAt).reversed())
                    .findFirst();
            if (latestMsgOpt.isPresent()) {
                DoubtMessageDto messageDto = modelMapper.map(latestMsgOpt.get(), DoubtMessageDto.class);
                dto.setMessage(messageDto);
            }
            return dto;
        }).collect(Collectors.toList());
    }


    // Helper method to map a DoubtConversation to its DTO.
    private DoubtConversationDto convertToDto(DoubtConversation conversation) {
        DoubtConversationDto dto = modelMapper.map(conversation, DoubtConversationDto.class);
        dto.setCourseTypeId(conversation.getCourseType().getId());
        // student details are mapped to a SenderStudentDetailDto in your DTO.
        dto.setStudent(modelMapper.map(conversation.getStudent(), SenderStudentDetailDto.class));
        if (conversation.getMessages() != null) {
            List<DoubtMessageDto> messageDtos = conversation.getMessages().stream()
                    .sorted(Comparator.comparing(DoubtMessage::getCreatedAt).reversed()) // sort descending
                    .map(msg -> modelMapper.map(msg, DoubtMessageDto.class))
                    .collect(Collectors.toList());
            dto.setMessages(messageDtos);
        }
        return dto;
    }

    // New helper method converting to DoubtConversationStudentsListDto.
    private DoubtConversationStudentsListDto convertToStudentsListDto(DoubtConversation conversation) {
        DoubtConversationStudentsListDto dto = new DoubtConversationStudentsListDto();
        dto.setConversationId(conversation.getId());
        dto.setStudent(modelMapper.map(conversation.getStudent(), SenderStudentDetailDto.class));
        return dto;
    }

    private DoubtCourseDetailDto convertToCourseDetailDto(CourseType courseType) {
        // Use modelMapper to map basic properties
        DoubtCourseDetailDto dto = modelMapper.map(courseType, DoubtCourseDetailDto.class);
        // Now set title and thumbnail from the associated Course
        if (courseType.getCourse() != null) {
            dto.setTitle(courseType.getCourse().getTitle());
            dto.setThumbnail(courseType.getCourse().getThumbnail());
        }
        return dto;
    }
}
