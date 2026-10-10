package com.example.demo.controller;

import com.example.demo.entity.Certificate;
import com.example.demo.entity.Course;
import com.example.demo.entity.User;
import com.example.demo.repository.CertificateRepo;
import com.example.demo.repository.CourseRepo;
import com.example.demo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class CertificateController {

    @Autowired
    private CertificateRepo certificateRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private UserRepo userRepo;

    // Student: List my earned certificates
    @GetMapping("/student/certificates")
    public ResponseEntity<List<Map<String, Object>>> getMyCertificates() {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).build();

        List<Certificate> certs = certificateRepo.findByStudentId(student.getId());
        List<Map<String, Object>> res = new ArrayList<>();
        for (Certificate c : certs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("certificateRef", c.getCertificateRef());
            map.put("courseName", c.getCourse() != null ? c.getCourse().getName() : "Course");
            map.put("courseCode", c.getCourse() != null ? c.getCourse().getCourseCode() : "");
            map.put("issuedAt", c.getIssuedAt());
            res.add(map);
        }
        return ResponseEntity.ok(res);
    }

    // Auto-issue certificate upon 100% progress
    @PostMapping("/student/courses/{courseId}/claim-certificate")
    public ResponseEntity<?> claimCertificate(@PathVariable Long courseId) {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).body("Not authenticated");

        Optional<Course> cOpt = courseRepo.findById(courseId);
        if (cOpt.isEmpty()) return ResponseEntity.notFound().build();

        Optional<Certificate> existing = certificateRepo.findByStudentIdAndCourseId(student.getId(), courseId);
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }

        Certificate cert = new Certificate();
        cert.setStudent(student);
        cert.setCourse(cOpt.get());
        cert.setCertificateRef("CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + LocalDateFormatter());
        cert.setIssuedAt(LocalDateTime.now());

        Certificate saved = certificateRepo.save(cert);
        return ResponseEntity.ok(saved);
    }

    // Render printable Certificate HTML page
    @GetMapping(value = "/certificates/{ref}/render", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> renderCertificate(@PathVariable String ref) {
        Optional<Certificate> certOpt = certificateRepo.findByCertificateRef(ref);
        if (certOpt.isEmpty()) {
            return ResponseEntity.status(404).body("<h2>Certificate not found.</h2>");
        }

        Certificate c = certOpt.get();
        String studentName = c.getStudent().getName() != null ? c.getStudent().getName() : c.getStudent().getEmail();
        String courseName = c.getCourse().getName();
        String dateStr = c.getIssuedAt().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Certificate of Completion</title>" +
                "<style>" +
                "@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&family=Playfair+Display:ital,wght@0,700;1,400&display=swap');" +
                "body { margin:0; padding:40px; background:#0f172a; font-family:'Plus Jakarta Sans', sans-serif; display:flex; justify-content:center; align-items:center; min-height:100vh; }" +
                ".cert-card { width:880px; background:#ffffff; border:12px solid #4f46e5; border-radius:16px; padding:60px; text-align:center; box-shadow:0 25px 50px -12px rgba(0,0,0,0.5); position:relative; }" +
                ".badge-seal { width:80px; height:80px; border-radius:50%; background:#4f46e5; color:#fff; display:inline-flex; align-items:center; justify-content:center; font-size:2.2rem; margin-bottom:20px; box-shadow:0 10px 15px -3px rgba(79,70,229,0.4); }" +
                "h1 { font-family:'Playfair Display', serif; font-size:3rem; color:#1e293b; margin:0 0 10px; letter-spacing:0.02em; }" +
                "p.subtitle { font-size:1.1rem; color:#64748b; margin:0 0 30px; text-transform:uppercase; letter-spacing:0.1em; font-weight:600; }" +
                "h2.student { font-family:'Playfair Display', serif; font-size:2.4rem; color:#4f46e5; margin:20px 0; font-weight:700; border-bottom:2px dashed #cbd5e1; display:inline-block; padding-bottom:8px; }" +
                "p.desc { font-size:1.15rem; color:#334155; line-height:1.6; max-width:640px; margin:20px auto 40px; }" +
                ".footer-row { display:flex; justify-content:space-between; align-items:flex-end; margin-top:50px; border-top:1px solid #e2e8f0; padding-top:20px; }" +
                ".sign { font-family:'Playfair Display', serif; font-style:italic; font-size:1.3rem; color:#0f172a; }" +
                "@media print { body { background:#fff; padding:0; } .cert-card { box-shadow:none; border-width:8px; } button { display:none; } }" +
                "</style></head><body>" +
                "<div class='cert-card'>" +
                "<div class='badge-seal'>🎓</div>" +
                "<h1>Certificate of Completion</h1>" +
                "<p class='subtitle'>Official E-Learning Credential</p>" +
                "<p style='color:#64748b; font-size:1rem; margin:0;'>This is proudly presented to</p>" +
                "<h2 class='student'>" + studentName + "</h2>" +
                "<p class='desc'>For successfully completing all requirements, video lectures, assignments, and curriculum criteria for <strong>" + courseName + "</strong>.</p>" +
                "<div class='footer-row'>" +
                "<div style='text-align:left;'><span style='font-size:0.85rem; color:#94a3b8;'>Issued On</span><br><strong>" + dateStr + "</strong></div>" +
                "<div><span class='sign'>Academic Board</span><br><span style='font-size:0.85rem; color:#94a3b8;'>Certified Instructor</span></div>" +
                "<div style='text-align:right;'><span style='font-size:0.85rem; color:#94a3b8;'>Verification ID</span><br><code style='font-weight:700; color:#4f46e5;'>" + c.getCertificateRef() + "</code></div>" +
                "</div>" +
                "<button onclick='window.print()' style='margin-top:30px; padding:10px 24px; background:#4f46e5; color:#fff; border:none; border-radius:8px; font-weight:600; cursor:pointer; font-size:0.95rem;'>🖨️ Print / Save as PDF</button>" +
                "</div></body></html>";

        return ResponseEntity.ok(html);
    }

    // Public Verification Endpoint
    @GetMapping("/certificates/verify/{ref}")
    public ResponseEntity<?> verifyCertificate(@PathVariable String ref) {
        Optional<Certificate> certOpt = certificateRepo.findByCertificateRef(ref);
        if (certOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("valid", false, "message", "Invalid credential ref"));

        Certificate c = certOpt.get();
        Map<String, Object> map = new HashMap<>();
        map.put("valid", true);
        map.put("studentName", c.getStudent().getName());
        map.put("courseName", c.getCourse().getName());
        map.put("issuedAt", c.getIssuedAt());
        map.put("certificateRef", c.getCertificateRef());
        return ResponseEntity.ok(map);
    }

    private String LocalDateFormatter() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}
