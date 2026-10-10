package com.example.demo.controller;

import com.example.demo.dto.PageResponse;
import com.example.demo.entity.Course;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.Payment;
import com.example.demo.entity.User;
import com.example.demo.enums.EnrollmentStatus;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repository.CourseRepo;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.PaymentRepo;
import com.example.demo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class PaymentController {

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    @Autowired
    private UserRepo userRepo;

    // Student: Initiate Payment Stub
    @PostMapping("/student/payments/initiate")
    public ResponseEntity<?> initiatePayment(@RequestBody Map<String, Object> req) {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).body("Not authenticated");

        Long courseId = req.get("courseId") != null ? Long.valueOf(req.get("courseId").toString()) : null;
        BigDecimal amount = req.get("amount") != null ? new BigDecimal(req.get("amount").toString()) : new BigDecimal("499.00");

        Course course = null;
        if (courseId != null) {
            course = courseRepo.findById(courseId).orElse(null);
        }

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setCourse(course);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setProviderRef("PAY_STUB_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        payment.setCreatedAt(LocalDateTime.now());

        Payment saved = paymentRepo.save(payment);

        /* ==========================================================================
         * NOTE / INTEGRATION POINT FOR LIVE PAYMENT GATEWAYS (Razorpay / Stripe):
         * --------------------------------------------------------------------------
         * RazorpayClient razorpay = new RazorpayClient("KEY_ID", "KEY_SECRET");
         * JSONObject orderRequest = new JSONObject();
         * orderRequest.put("amount", amount.multiply(new BigDecimal(100))); // paise
         * orderRequest.put("currency", "INR");
         * orderRequest.put("receipt", saved.getProviderRef());
         * Order order = razorpay.orders.create(orderRequest);
         * ========================================================================== */

        Map<String, Object> checkoutResponse = new HashMap<>();
        checkoutResponse.put("paymentId", saved.getId());
        checkoutResponse.put("providerRef", saved.getProviderRef());
        checkoutResponse.put("amount", saved.getAmount());
        checkoutResponse.put("currency", saved.getCurrency());
        checkoutResponse.put("status", saved.getStatus());
        checkoutResponse.put("mockCheckoutUrl", "/api/payments/webhook");
        checkoutResponse.put("instructions", "Use mock webhook or testing callback with providerRef to finalize transaction.");

        return ResponseEntity.ok(checkoutResponse);
    }

    // Webhook / Mock callback endpoint
    @PostMapping("/payments/webhook")
    public ResponseEntity<?> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        String providerRef = (String) payload.get("providerRef");
        String statusStr = (String) payload.getOrDefault("status", "COMPLETED");

        if (providerRef == null) {
            return ResponseEntity.badRequest().body("providerRef is required");
        }

        Optional<Payment> pOpt = paymentRepo.findByProviderRef(providerRef);
        if (pOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Payment payment = pOpt.get();
        if ("COMPLETED".equalsIgnoreCase(statusStr)) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());

            // Auto-enroll hook: If paid for a specific course, automatically enroll student
            if (payment.getCourse() != null && payment.getStudent() != null) {
                boolean alreadyEnrolled = enrollmentRepo
                        .findByStudentIdAndCourseId(payment.getStudent().getId(), payment.getCourse().getId())
                        .isPresent();

                if (!alreadyEnrolled) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setStudent(payment.getStudent());
                    enrollment.setCourse(payment.getCourse());
                    enrollment.setStatus(EnrollmentStatus.ACTIVE);
                    enrollment.setEnrolledAt(LocalDateTime.now());
                    enrollmentRepo.save(enrollment);
                }
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment updated = paymentRepo.save(payment);
        return ResponseEntity.ok(Map.of("message", "Payment processed successfully", "paymentId", updated.getId(), "status", updated.getStatus()));
    }

    // Student: View own payment receipts
    @GetMapping("/student/payments/mine")
    public ResponseEntity<List<Payment>> getMyPayments() {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(paymentRepo.findByStudentId(student.getId()));
    }

    // Admin: View all transactions (Paginated)
    @GetMapping("/admin/payments")
    public ResponseEntity<PageResponse<Payment>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Payment> all = paymentRepo.findAll();
        return ResponseEntity.ok(PageResponse.of(all, page, size));
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}
