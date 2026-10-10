package com.example.demo.repository;

import com.example.demo.entity.Quiz;
import com.example.demo.enums.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizRepo extends JpaRepository<Quiz, Long> {
    List<Quiz> findByModuleId(Long moduleId);
    List<Quiz> findByModuleIdAndStatus(Long moduleId, QuizStatus status);
}
