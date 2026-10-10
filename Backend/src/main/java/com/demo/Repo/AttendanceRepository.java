package com.example.demo.repository;

import com.example.demo.entity.Attendance;
import com.example.demo.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByLiveClassId(Long liveClassId);

    Optional<Attendance> findByLiveClassIdAndStudentId(Long liveClassId, Long studentId);

    List<Attendance> findByStudentIdAndLiveClassTimetableCourseId(Long studentId, Long courseId);

    List<Attendance> findByLiveClassTimetableCourseId(Long courseId);
}