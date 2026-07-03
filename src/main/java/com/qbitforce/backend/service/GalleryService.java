package com.qbitforce.backend.service;

import com.qbitforce.backend.dto.GalleryItemDto;
import com.qbitforce.backend.entity.GalleryItem;
import com.qbitforce.backend.repository.GalleryItemRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GalleryService {

    private final GalleryItemRepository repository;

    public GalleryService(GalleryItemRepository repository) {
        this.repository = repository;
    }

    public List<GalleryItemDto> listPublic() {
        return repository.findByActiveTrueOrderBySortOrderAscCreatedAtDesc().stream()
                .map(GalleryItemDto::fromEntity)
                .toList();
    }

    public List<GalleryItemDto> listAll() {
        return repository.findAll().stream().map(GalleryItemDto::fromEntity).toList();
    }

    public GalleryItemDto create(GalleryItemDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Gallery item already exists.");
        }
        return GalleryItemDto.fromEntity(repository.save(toEntity(dto)));
    }

    public GalleryItemDto update(String id, GalleryItemDto dto) {
        GalleryItem existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gallery item not found."));
        String newId = dto.id().trim();
        if (!id.equals(newId)) {
            if (repository.existsById(newId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Gallery item already exists.");
            }
            repository.delete(existing);
            GalleryItem item = toEntity(dto);
            item.setId(newId);
            return GalleryItemDto.fromEntity(repository.save(item));
        }
        apply(existing, dto);
        return GalleryItemDto.fromEntity(repository.save(existing));
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gallery item not found.");
        }
        repository.deleteById(id);
    }

    private GalleryItem toEntity(GalleryItemDto dto) {
        GalleryItem item = new GalleryItem();
        item.setId(dto.id());
        apply(item, dto);
        return item;
    }

    private void apply(GalleryItem item, GalleryItemDto dto) {
        item.setTitle(dto.title());
        item.setCaption(dto.caption());
        item.setCategory(dto.category());
        item.setImageUrl(dto.imageUrl());
        item.setSortOrder(dto.sortOrder());
        item.setActive(dto.active());
    }
}
