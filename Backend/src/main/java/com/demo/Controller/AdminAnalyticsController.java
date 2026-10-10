package com.example.demo.controller;

import com.example.demo.entity.Enrollment;
import com.example.demo.entity.Payment;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.enums.Role;
import com.example.demo.repository.CourseRepo;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.PaymentRepo;
import com.example.demo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    @Autowired
    private PaymentRepo paymentRepo;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        long totalUsers = userRepo.count();
        long totalCourses = courseRepo.count();
        long totalEnrollments = enrollmentRepo.count();

        List<Payment> completedPayments = paymentRepo.findByStatus(PaymentStatus.COMPLETED);
        BigDecimal totalRevenue = completedPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalUsers", totalUsers);
        overview.put("totalCourses", totalCourses);
        overview.put("totalEnrollments", totalEnrollments);
        overview.put("totalRevenue", totalRevenue);
        overview.put("studentsCount", userRepo.findByRole(Role.STUDENT).size());
        overview.put("teachersCount", userRepo.findByRole(Role.TEACHER).size());
        overview.put("adminsCount", userRepo.findByRole(Role.ADMIN).size());

        return ResponseEntity.ok(overview);
    }

    @GetMapping("/enrollments-trend")
    public ResponseEntity<Map<String, Long>> getEnrollmentsTrend() {
        List<Enrollment> enrollments = enrollmentRepo.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, Long> trend = enrollments.stream()
                .filter(e -> e.getEnrolledAt() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getEnrolledAt().format(formatter),
                        TreeMap::new,
                        Collectors.counting()
                ));

        return ResponseEntity.ok(trend);
    }

    @GetMapping("/revenue-trend")
    public ResponseEntity<Map<String, BigDecimal>> getRevenueTrend() {
        List<Payment> payments = paymentRepo.findByStatus(PaymentStatus.COMPLETED);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, BigDecimal> trend = payments.stream()
                .filter(p -> p.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCompletedAt().format(formatter),
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        return ResponseEntity.ok(trend);
    }
}
