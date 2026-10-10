package com.example.demo.service;

import com.example.demo.entity.Assignment;
import java.util.List;

public interface AssignmentService {

    Assignment createAssignment(Long moduleId, Assignment assignment, Long teacherId);

    Assignment updateAssignment(Long assignmentId, Assignment assignmentDetails, Long teacherId);

    void deleteAssignment(Long assignmentId, Long teacherId);

    List<Assignment> listAssignmentsByModule(Long moduleId);

    List<Assignment> listPublishedAssignmentsByModule(Long moduleId);
}