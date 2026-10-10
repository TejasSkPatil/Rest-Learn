package com.example.demo.service;

import com.example.demo.entity.Module;
import java.util.List;

public interface ModuleService {

    Module createModule(Long courseId, Module module, Long teacherId);

    Module updateModule(Long moduleId, Module moduleDetails, Long teacherId);

    void deleteModule(Long moduleId, Long teacherId);

    List<Module> listModulesByCourse(Long courseId);

    List<Module> listPublishedModulesByCourse(Long courseId);
}