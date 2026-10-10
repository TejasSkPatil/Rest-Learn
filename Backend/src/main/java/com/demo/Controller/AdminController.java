package com.example.demo.controller;

import com.example.demo.entity.AcademicSession;
import com.example.demo.entity.Department;
import com.example.demo.repository.AcademicSessionRepo;
import com.example.demo.repository.DepartmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private AcademicSessionRepo academicSessionRepo;

    // ==== Department CRUD ====

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> listDepartments() {
        return ResponseEntity.ok(departmentRepo.findAll());
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        return ResponseEntity.ok(departmentRepo.save(department));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @RequestBody Department departmentDetails) {
        Department department = departmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        department.setName(departmentDetails.getName());
        return ResponseEntity.ok(departmentRepo.save(department));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        departmentRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ==== Academic Session CRUD ====

    @GetMapping("/academic-sessions")
    public ResponseEntity<List<AcademicSession>> listAcademicSessions() {
        return ResponseEntity.ok(academicSessionRepo.findAll());
    }

    @PostMapping("/academic-sessions")
    public ResponseEntity<AcademicSession> createAcademicSession(@RequestBody AcademicSession session) {
        return ResponseEntity.ok(academicSessionRepo.save(session));
    }

    @PutMapping("/academic-sessions/{id}")
    public ResponseEntity<AcademicSession> updateAcademicSession(@PathVariable Long id, @RequestBody AcademicSession sessionDetails) {
        AcademicSession session = academicSessionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Academic session not found"));
        session.setName(sessionDetails.getName());
        session.setStartDate(sessionDetails.getStartDate());
        session.setEndDate(sessionDetails.getEndDate());
        return ResponseEntity.ok(academicSessionRepo.save(session));
    }

    @DeleteMapping("/academic-sessions/{id}")
    public ResponseEntity<?> deleteAcademicSession(@PathVariable Long id) {
        academicSessionRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}