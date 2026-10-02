package com.affycloud.TutorioBackend.services.implementation;


import com.affycloud.TutorioBackend.dtos.question.QuestionResponseDto;
import com.affycloud.TutorioBackend.dtos.question.QuestionUpdateDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.dtos.test.*;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestServiceImplementation implements TestService {
    @Autowired
      private TestRepository testRepository;

    @Autowired
    private CourseTypeRepository courseTypeRepository;

      @Autowired
    private UserRepository userRepository;

      @Autowired
    private TestQuestionRepository questionRepository;


    @Autowired
    private StudentTestResultRepository studentTestResultRepository;

    @Override
    public ApiResponse createTest(TestAddDto request, UUID userId) {
        // Validate the creator exists.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate that the user has the right role.
        if (user.getRole() != Role.TEACHER && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only teachers or admins can create tests");
        }

        Test test = new Test();
        test.setTitle(request.getTitle());
        test.setDescription(request.getDescription());
        test.setCreatedBy(user);

        // Validate and set the course type if provided.
        // (Assuming request.getCourseType() returns a UUID representing the course type)
        if (request.getCourseType() != null) {
            CourseType courseType = courseTypeRepository.findById(request.getCourseType())
                    .orElseThrow(() -> new IllegalArgumentException("Course type not found"));
            test.setCourseType(request.getCourseType());
        }

        // Map the questions.
        List<Question> questions = request.getQuestions().stream().map(qDto -> {
            Question question = new Question();
            question.setQuestionText(qDto.getQuestionText());
            question.setOptions(qDto.getOptions());
            question.setCorrectOptionIndex(qDto.getCorrectOptionIndex());
            question.setTest(test);
            return question;
        }).collect(Collectors.toList());

        test.setQuestions(questions);

        // Save the test.
        testRepository.save(test);

        return new ApiResponse("Test created successfully", false);
    }





    @Override
    public List<Test> getAllTests() {
        return testRepository.findAll(); 
    }

  @Override
public ApiResponse updateTest(UUID testId, TestUpdateDto request) {
    Test test = testRepository.findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("Test not found"));

    test.setTitle(request.getTitle());
    test.setDescription(request.getDescription());

    if (request.getCourseType() != null) {
        test.setCourseType(request.getCourseType());
    }

    // Update Questions
    Map<UUID, Question> existingQuestions = test.getQuestions()
            .stream()
            .collect(Collectors.toMap(Question::getId, q -> q));

    List<Question> updatedQuestions = new ArrayList<>();

    for (QuestionDto qDto : request.getQuestions()) {
        if (qDto.getId() != null && existingQuestions.containsKey(qDto.getId())) {
            // Update existing question
            Question question = existingQuestions.get(qDto.getId());
            question.setQuestionText(qDto.getQuestionText());
            question.setOptions(qDto.getOptions());
            question.setCorrectOptionIndex(qDto.getCorrectOptionIndex());
            updatedQuestions.add(question);
        } else {
            // Add new question
            Question newQuestion = new Question();
            newQuestion.setQuestionText(qDto.getQuestionText());
            newQuestion.setOptions(qDto.getOptions());
            newQuestion.setCorrectOptionIndex(qDto.getCorrectOptionIndex());
            newQuestion.setTest(test);
            updatedQuestions.add(newQuestion);
        }
    }

    test.getQuestions().clear();
    test.getQuestions().addAll(updatedQuestions); 

    testRepository.save(test);
    return new ApiResponse("Test updated successfully", false);
}


    @Override
public ApiResponseWithMeta getTestsByCourseType(UUID courseTypeId, UUID studentId) {
    List<Test> tests = testRepository.findByCourseType(courseTypeId,
            Sort.by(Sort.Direction.ASC, "createdAt"));
        Map<UUID, StudentTestResult> testResultMap = new HashMap<>();

        if (studentId != null) {
            List<StudentTestResult> results = studentTestResultRepository.findByStudentId(studentId);
            for (StudentTestResult result : results) {
                testResultMap.put(result.getTest().getId(), result);
            }
        }

        List<TestResultDto> testDtos = tests.stream()
                .map(test ->  {
                    StudentTestResult result = testResultMap.get(test.getId());
                    boolean attempted = result != null;
                    Integer score = (result != null) ? result.getScore() : null;

                    return new TestResultDto(
                            test.getId(),
                            test.getTitle(),
                            test.getDescription(),
                            attempted,
                            score
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> meta = new HashMap<>();
        meta.put("tests", testDtos);
        meta.put("count", testDtos.size());

    return new ApiResponseWithMeta("Tests retrieved successfully", false, meta);
}


@Override
public ApiResponseWithMeta getTestById(UUID testId) {
    Test test = testRepository.findById(testId)
        .orElseThrow(() -> new RuntimeException("Test not found"));

    TestResponseDto testResponse = new TestResponseDto();
    testResponse.setId(test.getId());
    testResponse.setTitle(test.getTitle());
    testResponse.setDescription(test.getDescription());
    testResponse.setCreatedAt(test.getCreatedAt());
    testResponse.setUpdatedAt(test.getUpdatedAt());

    List<QuestionResponseDto> questionDtos = test.getQuestions().stream().map(question -> {
        QuestionResponseDto questionDto = new QuestionResponseDto();
        questionDto.setId(question.getId());
        questionDto.setQuestionText(question.getQuestionText());
        questionDto.setOptions(question.getOptions());
        questionDto.setCorrectOptionIndex(question.getCorrectOptionIndex());
        return questionDto;
    }).toList();

    testResponse.setQuestions(questionDtos);

    Map<String, Object> meta = new HashMap<>();
    meta.put("test", testResponse);

    return new ApiResponseWithMeta("Test retrieved successfully", false, meta);
}


@Override
public ApiResponse updateQuestion(UUID questionId, QuestionUpdateDto request) {
    Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found"));

    question.setQuestionText(request.getQuestionText());
    question.setOptions(request.getOptions());
    question.setCorrectOptionIndex(request.getCorrectOptionIndex());

    questionRepository.save(question);

    return new ApiResponse("Question updated successfully", false);
}

@Override
public QuestionResponseDto getQuestionById(UUID questionId) {
    Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found"));

    return new QuestionResponseDto(
            question.getId(),
            question.getQuestionText(),
            question.getOptions(),
            question.getCorrectOptionIndex()
    );
}

}

