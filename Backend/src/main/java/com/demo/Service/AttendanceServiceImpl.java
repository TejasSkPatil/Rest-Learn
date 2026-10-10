package com.example.demo.service;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.LiveClass;
import com.example.demo.entity.User;
import com.example.demo.enums.AttendanceStatus;
import com.example.demo.enums.EnrollmentStatus;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.LiveClassRepository;
import com.example.demo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository AttendanceRepository;

    @Autowired
    private LiveClassRepository LiveClassRepository;

    @Autowired
    private EnrollmentRepository EnrollmentRepository;

    @Autowired
    private UserRepo userRepo;

    @Override
    public Attendance markAttendance(Long liveClassId, Long studentId, AttendanceStatus status, Long teacherId) {
        LiveClass liveClass = LiveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        // Verify teacher owns the course
        if (!liveClass.getTimetable().getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied");
        }

        Attendance attendance = AttendanceRepository.findByLiveClassIdAndStudentId(liveClassId, studentId)
                .orElse(new Attendance(liveClass, userRepo.findById(studentId).orElseThrow()));

        attendance.setStatus(status);
        attendance.setMarkedBy(userRepo.findById(teacherId).orElseThrow());
        attendance.setMarkedAt(LocalDateTime.now());
        attendance.setManuallySet(true);

        return AttendanceRepository.save(attendance);
    }

    @Override
    public void markAllPresent(Long liveClassId, Long teacherId) {
        LiveClass liveClass = LiveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        if (!liveClass.getTimetable().getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied");
        }

        Long courseId = liveClass.getTimetable().getCourse().getId();
        List<Enrollment> enrollments = EnrollmentRepository.findByStudentIdAndStatus(courseId, EnrollmentStatus.ACTIVE);

        for (Enrollment enrollment : enrollments) {
            Attendance attendance = AttendanceRepository.findByLiveClassIdAndStudentId(liveClassId, enrollment.getStudent().getId())
                    .orElse(new Attendance(liveClass, enrollment.getStudent()));
            attendance.setStatus(AttendanceStatus.PRESENT);
            attendance.setMarkedBy(userRepo.findById(teacherId).orElseThrow());
            attendance.setMarkedAt(LocalDateTime.now());
            attendance.setManuallySet(true);
            AttendanceRepository.save(attendance);
        }
    }

    @Override
    public List<Attendance> getAttendanceForLiveClass(Long liveClassId, Long teacherId) {
        LiveClass liveClass = LiveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        if (!liveClass.getTimetable().getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied");
        }

        Long courseId = liveClass.getTimetable().getCourse().getId();
        List<Enrollment> enrollments = EnrollmentRepository.findByStudentIdAndStatus(courseId, EnrollmentStatus.ACTIVE);

        List<Attendance> result = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Attendance attendance = AttendanceRepository.findByLiveClassIdAndStudentId(liveClassId, enrollment.getStudent().getId())
                    .orElse(null);
            if (attendance == null) {
                // Pre-fill with ABSENT
                attendance = new Attendance(liveClass, enrollment.getStudent());
                attendance = AttendanceRepository.save(attendance);
            }
            result.add(attendance);
        }

        return result;
    }
}