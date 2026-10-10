package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.enums.QuizStatus;
import com.example.demo.repository.*;
import com.example.demo.service.CourseAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/student/quizzes")
public class StudentQuizController {

    @Autowired
    private QuizRepo quizRepo;

    @Autowired
    private QuizResultRepo quizResultRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CourseAccessService courseAccessService;

    // Fetch quiz for taking (isCorrect hidden)
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuizForStudent(@PathVariable Long id) {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).body("Not authenticated");

        Optional<Quiz> qOpt = quizRepo.findById(id);
        if (qOpt.isEmpty()) return ResponseEntity.notFound().build();
        Quiz quiz = qOpt.get();

        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            return ResponseEntity.status(403).body("Quiz is not available");
        }

        // Sanitize correct answers
        Map<String, Object> sanitized = new HashMap<>();
        sanitized.put("id", quiz.getId());
        sanitized.put("title", quiz.getTitle());
        sanitized.put("timeLimitMinutes", quiz.getTimeLimitMinutes());

        List<Map<String, Object>> questionsList = new ArrayList<>();
        for (QuizQuestion q : quiz.getQuestions()) {
            Map<String, Object> qMap = new HashMap<>();
            qMap.put("id", q.getId());
            qMap.put("questionText", q.getQuestionText());
            qMap.put("position", q.getPosition());

            List<Map<String, Object>> ansList = new ArrayList<>();
            for (QuizAnswer a : q.getAnswers()) {
                Map<String, Object> aMap = new HashMap<>();
                aMap.put("id", a.getId());
                aMap.put("answerText", a.getAnswerText());
                ansList.add(aMap);
            }
            qMap.put("answers", ansList);
            questionsList.add(qMap);
        }
        sanitized.put("questions", questionsList);

        return ResponseEntity.ok(sanitized);
    }

    // Submit answers and compute score
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitQuiz(@PathVariable Long id, @RequestBody Map<Long, Long> selectedAnswers) {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).body("Not authenticated");

        Optional<Quiz> qOpt = quizRepo.findById(id);
        if (qOpt.isEmpty()) return ResponseEntity.notFound().build();
        Quiz quiz = qOpt.get();

        int score = 0;
        int total = quiz.getQuestions().size();

        for (QuizQuestion q : quiz.getQuestions()) {
            Long selectedAnswerId = selectedAnswers.get(q.getId());
            if (selectedAnswerId != null) {
                for (QuizAnswer a : q.getAnswers()) {
                    if (a.getId().equals(selectedAnswerId) && a.isCorrect()) {
                        score++;
                        break;
                    }
                }
            }
        }

        QuizResult result = new QuizResult();
        result.setQuiz(quiz);
        result.setStudent(student);
        result.setScore(score);
        result.setTotalQuestions(total);
        result.setSubmittedAt(LocalDateTime.now());

        QuizResult saved = quizResultRepo.save(result);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}/my-result")
    public ResponseEntity<?> getMyResult(@PathVariable Long id) {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).build();
        return quizResultRepo.findByQuizIdAndStudentId(id, student.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}
