package com.example.demo.controller;

import com.example.demo.entity.Module;
import com.example.demo.entity.Quiz;
import com.example.demo.entity.QuizAnswer;
import com.example.demo.entity.QuizQuestion;
import com.example.demo.entity.User;
import com.example.demo.enums.QuizStatus;
import com.example.demo.repository.ModuleRepository;
import com.example.demo.repository.QuizRepo;
import com.example.demo.repository.QuizResultRepo;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.CourseAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/teacher/quizzes")
public class TeacherQuizController {

    private final QuizRepo quizRepo;
    private final ModuleRepository moduleRepository;
    private final QuizResultRepo quizResultRepo;
    private final UserRepo userRepo;
    private final CourseAccessService courseAccessService;

    public TeacherQuizController(QuizRepo quizRepo,
                                 ModuleRepository moduleRepository,
                                 QuizResultRepo quizResultRepo,
                                 UserRepo userRepo,
                                 CourseAccessService courseAccessService) {
        this.quizRepo = quizRepo;
        this.moduleRepository = moduleRepository;
        this.quizResultRepo = quizResultRepo;
        this.userRepo = userRepo;
        this.courseAccessService = courseAccessService;
    }

    @PostMapping("/module/{moduleId}")
    public ResponseEntity<?> createQuiz(@PathVariable Long moduleId, @RequestBody Quiz quizRequest) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        Optional<Module> moduleOpt = moduleRepository.findById(moduleId);
        if (moduleOpt.isEmpty()) return ResponseEntity.badRequest().body("Module not found");

        Module module = moduleOpt.get();
        if (!courseAccessService.isTeacherOwner(module.getCourse().getId(), teacher.getId())) {
            return ResponseEntity.status(403).body("You do not own this course");
        }

        quizRequest.setModule(module);
        if (quizRequest.getQuestions() != null) {
            for (QuizQuestion q : quizRequest.getQuestions()) {
                q.setQuiz(quizRequest);
                if (q.getAnswers() != null) {
                    for (QuizAnswer a : q.getAnswers()) {
                        a.setQuestion(q);
                    }
                }
            }
        }

        Quiz saved = quizRepo.save(quizRequest);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<?> listQuizzesByModule(@PathVariable Long moduleId) {
        return ResponseEntity.ok(quizRepo.findByModuleId(moduleId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuiz(@PathVariable Long id) {
        return quizRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam QuizStatus status) {
        Optional<Quiz> qOpt = quizRepo.findById(id);
        if (qOpt.isEmpty()) return ResponseEntity.notFound().build();
        Quiz q = qOpt.get();
        q.setStatus(status);
        return ResponseEntity.ok(quizRepo.save(q));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id) {
        quizRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<?> getResults(@PathVariable Long id) {
        return ResponseEntity.ok(quizResultRepo.findByQuizId(id));
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}
