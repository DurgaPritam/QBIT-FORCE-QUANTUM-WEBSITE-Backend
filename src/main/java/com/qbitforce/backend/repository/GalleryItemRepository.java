package com.qbitforce.backend.repository;

import com.qbitforce.backend.entity.GalleryItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryItemRepository extends JpaRepository<GalleryItem, String> {
    List<GalleryItem> findByActiveTrueOrderBySortOrderAscCreatedAtDesc();
}
