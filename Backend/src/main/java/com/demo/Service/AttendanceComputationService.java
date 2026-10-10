package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.enums.AttendanceStatus;
import com.example.demo.enums.EnrollmentStatus;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceComputationService {

    @Autowired
    private AttendanceRepository AttendanceRepository;

    @Autowired
    private LiveClassJoinEventRepo joinEventRepo;

    @Autowired
    private EnrollmentRepository EnrollmentRepository;

    @Autowired
    private AttendanceRuleRepository ruleRepository;

    /**
     * Computes attendance for all enrolled students in a live class based on join/leave events.
     * Skips records that were manually set by a teacher.
     */
    public void computeForLiveClass(Long liveClassId) {
        LiveClass liveClass = LiveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        Long courseId = liveClass.getTimetable().getCourse().getId();
        AttendanceRule rule = ruleRepository.findByCourseId(courseId)
                .orElse(ruleRepository.findByCourseIsNull().orElse(new AttendanceRule(15)));

        List<Enrollment> enrollments = EnrollmentRepository.findByStudentIdAndStatus(courseId, EnrollmentStatus.ACTIVE);

        for (Enrollment enrollment : enrollments) {
            Attendance attendance = AttendanceRepository.findByLiveClassIdAndStudentId(liveClassId, enrollment.getStudent().getId())
                    .orElse(null);

            // Skip manually set records
            if (attendance != null && attendance.isManuallySet()) {
                continue;
            }

            List<LiveClassJoinEvent> events = joinEventRepo.findByLiveClassIdAndStudentId(liveClassId, enrollment.getStudent().getId());

            if (events.isEmpty()) {
                // No join event → ABSENT
                if (attendance == null) {
                    attendance = new Attendance(liveClass, enrollment.getStudent());
                }
                attendance.setStatus(AttendanceStatus.ABSENT);
                attendance.setDurationMinutes(0);
            } else {
                LiveClassJoinEvent firstEvent = events.get(0);
                LocalDateTime joinTime = firstEvent.getJoinTime();
                LocalDateTime leaveTime = firstEvent.getLeaveTime() != null ? firstEvent.getLeaveTime() : liveClass.getEndedAt();

                Duration duration = Duration.between(joinTime, leaveTime);
                int durationMinutes = (int) duration.toMinutes();

                // Check if joined within window
                LocalDateTime classStart = liveClass.getDate().atTime(liveClass.getTimetable().getStartTime());
                long minutesLate = Duration.between(classStart, joinTime).toMinutes();

                AttendanceStatus status;
                if (minutesLate > rule.getWindowMinutes()) {
                    status = AttendanceStatus.LATE;
                } else if (rule.getMinDurationMinutes() != null && durationMinutes < rule.getMinDurationMinutes()) {
                    status = AttendanceStatus.LATE;
                } else {
                    status = AttendanceStatus.PRESENT;
                }

                if (attendance == null) {
                    attendance = new Attendance(liveClass, enrollment.getStudent());
                }
                attendance.setStatus(status);
                attendance.setJoinTime(joinTime);
                attendance.setLeaveTime(leaveTime);
                attendance.setDurationMinutes(durationMinutes);
            }

            AttendanceRepository.save(attendance);
        }
    }

    @Autowired
    private LiveClassRepository LiveClassRepository;
}