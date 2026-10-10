package com.example.demo.service;

import com.example.demo.entity.Attendance;
import com.example.demo.enums.AttendanceStatus;
import java.util.List;

public interface AttendanceService {

    Attendance markAttendance(Long liveClassId, Long studentId, AttendanceStatus status, Long teacherId);

    void markAllPresent(Long liveClassId, Long teacherId);

    List<Attendance> getAttendanceForLiveClass(Long liveClassId, Long teacherId);
}