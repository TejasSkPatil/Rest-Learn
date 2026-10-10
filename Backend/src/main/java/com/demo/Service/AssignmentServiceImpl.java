package com.example.demo.service;

import com.example.demo.entity.Assignment;
import com.example.demo.entity.Module;
import com.example.demo.enums.AssignmentStatus;
import com.example.demo.repository.AssignmentRepository;
import com.example.demo.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    @Autowired
    private AssignmentRepository AssignmentRepository;

    @Autowired
    private ModuleRepository ModuleRepository;

    @Override
    public Assignment createAssignment(Long moduleId, Assignment assignment, Long teacherId) {
        Module module = ModuleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        if (!module.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied: you do not own this course");
        }
        assignment.setModule(module);
        return AssignmentRepository.save(assignment);
    }

    @Override
    public Assignment updateAssignment(Long assignmentId, Assignment assignmentDetails, Long teacherId) {
        Assignment assignment = AssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        if (!assignment.getModule().getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied: you do not own this course");
        }
        assignment.setTitle(assignmentDetails.getTitle());
        assignment.setDescription(assignmentDetails.getDescription());
        assignment.setDueDate(assignmentDetails.getDueDate());
        assignment.setMaxMarks(assignmentDetails.getMaxMarks());
        if (assignmentDetails.getStatus() != null) {
            assignment.setStatus(assignmentDetails.getStatus());
        }
        return AssignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignment(Long assignmentId, Long teacherId) {
        Assignment assignment = AssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        if (!assignment.getModule().getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied: you do not own this course");
        }
        AssignmentRepository.delete(assignment);
    }

    @Override
    public List<Assignment> listAssignmentsByModule(Long moduleId) {
        return AssignmentRepository.findByModuleId(moduleId);
    }

    @Override
    public List<Assignment> listPublishedAssignmentsByModule(Long moduleId) {
        return AssignmentRepository.findByModuleIdAndStatus(moduleId, AssignmentStatus.PUBLISHED);
    }
}