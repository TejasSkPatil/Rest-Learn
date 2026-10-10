package com.example.demo.repository;

import com.example.demo.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepo extends JpaRepository<Certificate, Long> {
    List<Certificate> findByStudentId(Long studentId);
    Optional<Certificate> findByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<Certificate> findByCertificateRef(String certificateRef);
}
