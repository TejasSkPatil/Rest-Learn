package com.example.demo.repository;

import com.example.demo.entity.LiveClass;
import com.example.demo.enums.LiveClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LiveClassRepository extends JpaRepository<LiveClass, Long> {

    Optional<LiveClass> findByTimetableIdAndDate(Long timetableId, LocalDate date);

    List<LiveClass> findByTimetableId(Long timetableId);

    @Query("SELECT lc FROM LiveClass lc WHERE lc.timetable.teacher.id = :teacherId AND lc.date = :date")
    List<LiveClass> findTodaysSessionsForTeacher(@Param("teacherId") Long teacherId, @Param("date") LocalDate date);

    @Query("SELECT lc FROM LiveClass lc WHERE lc.timetable.course.id IN " +
           "(SELECT e.course.id FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ACTIVE') " +
           "AND lc.date = :date")
    List<LiveClass> findTodaysSessionsForStudent(@Param("studentId") Long studentId, @Param("date") LocalDate date);
    @Query("SELECT lc FROM LiveClass lc WHERE lc.timetable.course.id = :courseId")
    List<LiveClass> findByTimetableCourseId(@Param("courseId") Long courseId);
}