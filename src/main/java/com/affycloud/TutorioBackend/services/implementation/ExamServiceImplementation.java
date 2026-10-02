package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.exam.*;
import com.affycloud.TutorioBackend.models.Exam;
import com.affycloud.TutorioBackend.repositories.ExamRepository;
import com.affycloud.TutorioBackend.services.ExamService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExamServiceImplementation implements ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ExamResponseDTO createExam(ExamDTO examDTO) {

        // Map incoming DTO to entity
        Exam exam = modelMapper.map(examDTO, Exam.class);
        // Set exam reference for each question (if questions exist)
        if (exam.getQuestions() != null) {
            exam.getQuestions().forEach(q -> q.setExam(exam));
        }


        Exam savedExam = examRepository.save(exam);
        return modelMapper.map(savedExam, ExamResponseDTO.class);


        }

    @Override
    public List<ExamResponseDTO> getAllExams() {
        List<Exam> exams = examRepository.findAll();
        return exams.stream()
                .map(exam -> modelMapper.map(exam, ExamResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ExamResponseDTO getExamById(UUID id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));
        return modelMapper.map(exam, ExamResponseDTO.class);
    }


    @Override
    public boolean updateExam(UUID id, ExamDTO examDTO) {
        Optional<Exam> existingExam = examRepository.findById(id);
        if (existingExam.isPresent()) {
            Exam exam = existingExam.get();
            exam.setTitle(examDTO.getTitle());
            return examRepository.save(exam) != null;
        }
        return false;
    }

    @Override
    public boolean deleteExam(UUID id) {
        Optional<Exam> exam = examRepository.findById(id);
        if (exam.isPresent()) {
            examRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public ExamQuestionsByExamResponseDto getQuestionsByExamId(UUID examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));

        List<ExamQuestionDTO> questionDTOs = exam.getQuestions().stream().map(q -> {
            ExamQuestionDTO dto = new ExamQuestionDTO();
            dto.setId(q.getId());
            dto.setQuestionText(q.getQuestionText());
            return dto;
        }).collect(Collectors.toList());

        return new ExamQuestionsByExamResponseDto(examId, questionDTOs);
    }
}
