package com.example.demo.repository;

import com.example.demo.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseReviewRepo extends JpaRepository<CourseReview, Long> {
    List<CourseReview> findByCourseId(Long courseId);
    Optional<CourseReview> findByCourseIdAndStudentId(Long courseId, Long studentId);

    @Query("SELECT AVG(r.rating) FROM CourseReview r WHERE r.course.id = :courseId")
    Double getAverageRating(Long courseId);

    @Query("SELECT COUNT(r) FROM CourseReview r WHERE r.course.id = :courseId")
    Long countReviews(Long courseId);
}
