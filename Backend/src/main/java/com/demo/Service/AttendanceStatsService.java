package com.example.demo.service;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.LiveClass;
import com.example.demo.enums.AttendanceStatus;
import com.example.demo.enums.EnrollmentStatus;
import com.example.demo.enums.LiveClassStatus;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.LiveClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceStatsService {

    @Autowired
    private AttendanceRepository AttendanceRepository;

    @Autowired
    private EnrollmentRepository EnrollmentRepository;

    @Autowired
    private LiveClassRepository LiveClassRepository;

    /**
     * Get attendance percentage for a student in a specific course.
     * Counts PRESENT as 1, LATE as 0.5, ABSENT as 0.
     * Excludes CANCELLED live classes.
     */
    public double getStudentCourseAttendance(Long studentId, Long courseId) {
        List<Attendance> records = AttendanceRepository.findByStudentIdAndLiveClassTimetableCourseId(studentId, courseId);

        if (records.isEmpty()) return 0.0;

        double total = 0;
        int held = 0;
        for (Attendance a : records) {
            if (a.getLiveClass().getStatus() == LiveClassStatus.CANCELLED) continue;
            held++;
            if (a.getStatus() == AttendanceStatus.PRESENT) total += 1.0;
            else if (a.getStatus() == AttendanceStatus.LATE) total += 0.5;
        }

        return held == 0 ? 0.0 : (total / held) * 100;
    }

    /**
     * Get overall attendance percentage across all enrolled courses.
     */
    public double getStudentOverallAttendance(Long studentId) {
        List<Enrollment> enrollments = EnrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);
        if (enrollments.isEmpty()) return 0.0;

        double total = 0;
        for (Enrollment e : enrollments) {
            total += getStudentCourseAttendance(studentId, e.getCourse().getId());
        }
        return total / enrollments.size();
    }

    /**
     * Get per-course breakdown for a student.
     */
    public Map<String, Object> getStudentAttendanceBreakdown(Long studentId) {
        List<Enrollment> enrollments = EnrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);
        Map<String, Object> result = new HashMap<>();
        Map<String, Double> perCourse = new HashMap<>();

        for (Enrollment e : enrollments) {
            perCourse.put(e.getCourse().getName(), getStudentCourseAttendance(studentId, e.getCourse().getId()));
        }

        result.put("perCourse", perCourse);
        result.put("overall", getStudentOverallAttendance(studentId));
        return result;
    }

    /**
     * Get attendance summary for a course (for teacher).
     */
    public Map<String, Object> getCourseAttendanceSummary(Long courseId) {
        List<LiveClass> liveClasses = LiveClassRepository.findByTimetableCourseId(courseId);
        int totalSessions = 0;
        int presentCount = 0;
        int absentCount = 0;

        for (LiveClass lc : liveClasses) {
            if (lc.getStatus() == LiveClassStatus.CANCELLED) continue;
            totalSessions++;
            List<Attendance> records = AttendanceRepository.findByLiveClassId(lc.getId());
            for (Attendance a : records) {
                if (a.getStatus() == AttendanceStatus.PRESENT) presentCount++;
                else absentCount++;
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSessions", totalSessions);
        summary.put("presentCount", presentCount);
        summary.put("absentCount", absentCount);
        summary.put("average", totalSessions == 0 ? 0.0 : ((double) presentCount / (presentCount + absentCount)) * 100);
        return summary;
    }
}