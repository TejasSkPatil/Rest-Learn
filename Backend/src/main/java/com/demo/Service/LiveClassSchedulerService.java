package com.example.demo.service;

import com.example.demo.entity.LiveClass;
import com.example.demo.entity.Timetable;
import com.example.demo.repository.LiveClassRepository;
import com.example.demo.repository.TimetableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LiveClassSchedulerService {

    @Autowired
    private LiveClassRepository LiveClassRepository;

    @Autowired
    private TimetableRepository TimetableRepository;

    @Value("${app.live-classes.generate-days:14}")
    private int generateDays;

    /**
     * For each Timetable entry, creates LiveClass rows for the next N days
     * matching that day-of-week, if they don't already exist.
     */
    public int generateUpcomingSessions() {
        List<Timetable> allEntries = TimetableRepository.findAll();
        int created = 0;
        LocalDate today = LocalDate.now();

        for (Timetable entry : allEntries) {
            DayOfWeek targetDay = DayOfWeek.valueOf(entry.getDayOfWeek().toUpperCase());

            for (int i = 0; i < generateDays; i++) {
                LocalDate date = today.plusDays(i);
                if (date.getDayOfWeek() == targetDay) {
                    // Check if already exists
                    if (LiveClassRepository.findByTimetableIdAndDate(entry.getId(), date).isEmpty()) {
                        LiveClass liveClass = new LiveClass(entry, date);
                        LiveClassRepository.save(liveClass);
                        created++;
                    }
                }
            }
        }

        return created;
    }
}