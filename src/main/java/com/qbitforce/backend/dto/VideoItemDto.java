package com.qbitforce.backend.dto;

import com.qbitforce.backend.entity.VideoItem;

public record VideoItemDto(
        String id,
        String title,
        String description,
        String duration,
        String category,
        String src,
        String youtubeId,
        String thumbnail,
        int sortOrder,
        boolean active) {

    public static VideoItemDto fromEntity(VideoItem item) {
        return new VideoItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getDuration(),
                item.getCategory(),
                item.getSrc(),
                item.getYoutubeId(),
                item.getThumbnail(),
                item.getSortOrder(),
                item.isActive());
    }
}
