package com.example.demo.repository;

import com.example.demo.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalTime;
import java.util.List;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    List<Timetable> findByTeacherId(Long teacherId);

    List<Timetable> findByCourseId(Long courseId);

    List<Timetable> findByAcademicSessionId(Long sessionId);

    // Find overlapping entries for the same teacher on the same day
    @Query("SELECT t FROM Timetable t WHERE t.teacher.id = :teacherId AND t.dayOfWeek = :dayOfWeek " +
           "AND t.id != :excludeId " +
           "AND ((t.startTime < :endTime AND t.endTime > :startTime))")
    List<Timetable> findOverlappingTeacherEntries(
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);

    // Find overlapping entries for the same course on the same day
    @Query("SELECT t FROM Timetable t WHERE t.course.id = :courseId AND t.dayOfWeek = :dayOfWeek " +
           "AND t.id != :excludeId " +
           "AND ((t.startTime < :endTime AND t.endTime > :startTime))")
    List<Timetable> findOverlappingCourseEntries(
            @Param("courseId") Long courseId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);
}