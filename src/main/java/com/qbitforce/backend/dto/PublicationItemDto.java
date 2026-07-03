package com.qbitforce.backend.dto;

import com.qbitforce.backend.entity.PublicationItem;
import java.time.LocalDate;

public record PublicationItemDto(
        String id,
        String title,
        String excerpt,
        String date,
        String category,
        String readTime,
        String author,
        boolean featured,
        String imageUrl,
        String link,
        int sortOrder,
        boolean active) {

    public static PublicationItemDto fromEntity(PublicationItem item) {
        return new PublicationItemDto(
                item.getId(),
                item.getTitle(),
                item.getExcerpt(),
                item.getPublishDate().toString(),
                item.getCategory(),
                item.getReadTime(),
                item.getAuthor(),
                item.isFeatured(),
                item.getImageUrl(),
                item.getLink(),
                item.getSortOrder(),
                item.isActive());
    }

    public PublicationItem toEntity() {
        PublicationItem item = new PublicationItem();
        item.setId(id);
        item.setTitle(title);
        item.setExcerpt(excerpt);
        item.setPublishDate(LocalDate.parse(date));
        item.setCategory(category);
        item.setReadTime(readTime != null ? readTime : "3 min");
        item.setAuthor(author);
        item.setFeatured(featured);
        item.setImageUrl(imageUrl);
        item.setLink(link);
        item.setSortOrder(sortOrder);
        item.setActive(active);
        return item;
    }
}
