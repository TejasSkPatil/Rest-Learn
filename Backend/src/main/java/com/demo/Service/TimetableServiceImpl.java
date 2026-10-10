package com.example.demo.service;

import com.example.demo.entity.Enrollment;
import com.example.demo.entity.Timetable;
import com.example.demo.enums.EnrollmentStatus;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.TimetableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimetableServiceImpl implements TimetableService {

    @Autowired
    private TimetableRepository TimetableRepository;

    @Autowired
    private EnrollmentRepository EnrollmentRepository;

    @Override
    public Timetable createEntry(Timetable timetable) {
        // Check for teacher schedule conflict
        List<Timetable> teacherConflicts = TimetableRepository.findOverlappingTeacherEntries(
                timetable.getTeacher().getId(),
                timetable.getDayOfWeek(),
                timetable.getStartTime(),
                timetable.getEndTime(),
                null
        );

        if (!teacherConflicts.isEmpty()) {
            throw new RuntimeException("Teacher schedule conflict: overlapping entry exists for this time slot");
        }

        // Check for course double-booking
        List<Timetable> courseConflicts = TimetableRepository.findOverlappingCourseEntries(
                timetable.getCourse().getId(),
                timetable.getDayOfWeek(),
                timetable.getStartTime(),
                timetable.getEndTime(),
                null
        );

        if (!courseConflicts.isEmpty()) {
            throw new RuntimeException("Course schedule conflict: this course is already scheduled in an overlapping time slot");
        }

        return TimetableRepository.save(timetable);
    }

    @Override
    public Timetable updateEntry(Long id, Timetable timetableDetails) {
        Timetable existing = TimetableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timetable entry not found"));

        // Check for teacher schedule conflict (excluding current entry)
        List<Timetable> teacherConflicts = TimetableRepository.findOverlappingTeacherEntries(
                timetableDetails.getTeacher().getId(),
                timetableDetails.getDayOfWeek(),
                timetableDetails.getStartTime(),
                timetableDetails.getEndTime(),
                id
        );

        if (!teacherConflicts.isEmpty()) {
            throw new RuntimeException("Teacher schedule conflict: overlapping entry exists for this time slot");
        }

        // Check for course double-booking (excluding current entry)
        List<Timetable> courseConflicts = TimetableRepository.findOverlappingCourseEntries(
                timetableDetails.getCourse().getId(),
                timetableDetails.getDayOfWeek(),
                timetableDetails.getStartTime(),
                timetableDetails.getEndTime(),
                id
        );

        if (!courseConflicts.isEmpty()) {
            throw new RuntimeException("Course schedule conflict: this course is already scheduled in an overlapping time slot");
        }

        existing.setCourse(timetableDetails.getCourse());
        existing.setTeacher(timetableDetails.getTeacher());
        existing.setAcademicSession(timetableDetails.getAcademicSession());
        existing.setDayOfWeek(timetableDetails.getDayOfWeek());
        existing.setStartTime(timetableDetails.getStartTime());
        existing.setEndTime(timetableDetails.getEndTime());

        return TimetableRepository.save(existing);
    }

    @Override
    public void deleteEntry(Long id) {
        TimetableRepository.deleteById(id);
    }

    @Override
    public List<Timetable> getTimetable(String filterBy, Long filterId) {
        if (filterBy == null || filterId == null) {
            return TimetableRepository.findAll();
        }
        switch (filterBy) {
            case "course":
                return TimetableRepository.findByCourseId(filterId);
            case "teacher":
                return TimetableRepository.findByTeacherId(filterId);
            case "session":
                return TimetableRepository.findByAcademicSessionId(filterId);
            default:
                return TimetableRepository.findAll();
        }
    }

    @Override
    public List<Timetable> getMyTimetableAsTeacher(Long teacherId) {
        return TimetableRepository.findByTeacherId(teacherId);
    }

    @Override
    public List<Timetable> getMyTimetableAsStudent(Long studentId) {
        // Get all active enrollments for the student
        List<Enrollment> enrollments = EnrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);
        List<Long> courseIds = enrollments.stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return List.of();
        }

        // Get timetable entries for all enrolled courses
        return TimetableRepository.findAll().stream()
                .filter(t -> courseIds.contains(t.getCourse().getId()))
                .collect(Collectors.toList());
    }
}