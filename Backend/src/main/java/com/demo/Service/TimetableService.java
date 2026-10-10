package com.example.demo.service;

import com.example.demo.entity.Timetable;
import java.util.List;

public interface TimetableService {

    Timetable createEntry(Timetable timetable);

    Timetable updateEntry(Long id, Timetable timetableDetails);

    void deleteEntry(Long id);

    List<Timetable> getTimetable(String filterBy, Long filterId);

    List<Timetable> getMyTimetableAsTeacher(Long teacherId);

    List<Timetable> getMyTimetableAsStudent(Long studentId);
}