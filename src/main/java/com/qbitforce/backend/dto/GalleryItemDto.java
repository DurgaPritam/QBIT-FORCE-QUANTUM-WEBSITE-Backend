package com.qbitforce.backend.dto;

import com.qbitforce.backend.entity.GalleryItem;

public record GalleryItemDto(
        String id,
        String title,
        String caption,
        String category,
        String imageUrl,
        int sortOrder,
        boolean active) {

    public static GalleryItemDto fromEntity(GalleryItem item) {
        return new GalleryItemDto(
                item.getId(),
                item.getTitle(),
                item.getCaption(),
                item.getCategory(),
                item.getImageUrl(),
                item.getSortOrder(),
                item.isActive());
    }
}
