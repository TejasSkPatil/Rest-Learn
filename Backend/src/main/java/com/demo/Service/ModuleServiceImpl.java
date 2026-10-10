package com.example.demo.service;

import com.example.demo.entity.Course;
import com.example.demo.entity.Module;
import com.example.demo.enums.ModuleStatus;
import com.example.demo.repository.CourseRepo;
import com.example.demo.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleRepository ModuleRepository;

    @Autowired
    private CourseRepo courseRepo;

    @Override
    public Module createModule(Long courseId, Module module, Long teacherId) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied: you do not own this course");
        }
        module.setCourse(course);
        return ModuleRepository.save(module);
    }

    @Override
    public Module updateModule(Long moduleId, Module moduleDetails, Long teacherId) {
        Module module = ModuleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        if (!module.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied: you do not own this course");
        }
        module.setTitle(moduleDetails.getTitle());
        module.setPosition(moduleDetails.getPosition());
        if (moduleDetails.getStatus() != null) {
            module.setStatus(moduleDetails.getStatus());
        }
        return ModuleRepository.save(module);
    }

    @Override
    public void deleteModule(Long moduleId, Long teacherId) {
        Module module = ModuleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        if (!module.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied: you do not own this course");
        }
        ModuleRepository.delete(module);
    }

    @Override
    public List<Module> listModulesByCourse(Long courseId) {
        return ModuleRepository.findByCourseIdOrderByPosition(courseId);
    }

    @Override
    public List<Module> listPublishedModulesByCourse(Long courseId) {
        return ModuleRepository.findByCourseIdAndStatusOrderByPosition(courseId, ModuleStatus.PUBLISHED);
    }
}