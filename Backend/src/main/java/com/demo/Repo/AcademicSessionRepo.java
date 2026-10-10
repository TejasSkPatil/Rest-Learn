package com.example.demo.repository;

import com.example.demo.entity.AcademicSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicSessionRepo extends JpaRepository<AcademicSession, Long> {
}