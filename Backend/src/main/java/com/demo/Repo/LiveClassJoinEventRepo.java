package com.example.demo.repository;

import com.example.demo.entity.LiveClassJoinEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LiveClassJoinEventRepo extends JpaRepository<LiveClassJoinEvent, Long> {

    List<LiveClassJoinEvent> findByLiveClassId(Long liveClassId);

    List<LiveClassJoinEvent> findByLiveClassIdAndStudentId(Long liveClassId, Long studentId);
}