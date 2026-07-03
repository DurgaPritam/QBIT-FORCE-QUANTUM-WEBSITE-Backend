package com.qbitforce.backend.dto;

import com.qbitforce.backend.entity.PressMediaItem;

public record PressMediaItemDto(
        String id,
        String title,
        String caption,
        String category,
        String imageUrl,
        int sortOrder,
        boolean active) {

    public static PressMediaItemDto fromEntity(PressMediaItem item) {
        return new PressMediaItemDto(
                item.getId(),
                item.getTitle(),
                item.getCaption(),
                item.getCategory(),
                item.getImageUrl(),
                item.getSortOrder(),
                item.isActive());
    }
}
