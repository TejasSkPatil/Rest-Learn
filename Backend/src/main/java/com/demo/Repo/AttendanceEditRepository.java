package com.example.demo.repository;

import com.example.demo.entity.AttendanceEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceEditRepository extends JpaRepository<AttendanceEdit, Long> {

    List<AttendanceEdit> findByAttendanceIdOrderByEditedAtDesc(Long attendanceId);
}