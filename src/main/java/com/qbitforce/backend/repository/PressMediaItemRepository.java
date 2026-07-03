package com.qbitforce.backend.repository;

import com.qbitforce.backend.entity.PressMediaItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PressMediaItemRepository extends JpaRepository<PressMediaItem, String> {
    List<PressMediaItem> findByActiveTrueOrderBySortOrderAscCreatedAtDesc();
}
