package com.qbitforce.backend.repository;

import com.qbitforce.backend.entity.VideoItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoItemRepository extends JpaRepository<VideoItem, String> {
    List<VideoItem> findByActiveTrueOrderBySortOrderAscCreatedAtDesc();
}
