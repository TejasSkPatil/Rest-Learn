package com.example.demo.repository;

import com.example.demo.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizResultRepo extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByQuizId(Long quizId);
    Optional<QuizResult> findByQuizIdAndStudentId(Long quizId, Long studentId);
}
