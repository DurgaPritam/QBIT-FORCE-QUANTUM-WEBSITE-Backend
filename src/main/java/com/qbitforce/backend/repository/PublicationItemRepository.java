package com.qbitforce.backend.repository;

import com.qbitforce.backend.entity.PublicationItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationItemRepository extends JpaRepository<PublicationItem, String> {
    List<PublicationItem> findByActiveTrueOrderBySortOrderAscPublishDateDesc();
}
