package com.example.demo.repository;

import com.example.demo.entity.ProgressItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgressItemRepository extends JpaRepository<ProgressItem, Long> {

    List<ProgressItem> findByStudentIdAndItemType(Long studentId, String itemType);

    boolean existsByStudentIdAndItemTypeAndItemId(Long studentId, String itemType, Long itemId);
}