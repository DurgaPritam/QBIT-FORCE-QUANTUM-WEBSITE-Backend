package com.qbitforce.backend.service;

import com.qbitforce.backend.dto.PublicationItemDto;
import com.qbitforce.backend.entity.PublicationItem;
import com.qbitforce.backend.repository.PublicationItemRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PublicationService {

    private final PublicationItemRepository repository;

    public PublicationService(PublicationItemRepository repository) {
        this.repository = repository;
    }

    public List<PublicationItemDto> listPublic() {
        return repository.findByActiveTrueOrderBySortOrderAscPublishDateDesc().stream()
                .map(PublicationItemDto::fromEntity)
                .toList();
    }

    public List<PublicationItemDto> listAll() {
        return repository.findAll().stream().map(PublicationItemDto::fromEntity).toList();
    }

    public PublicationItemDto create(PublicationItemDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Publication already exists.");
        }
        return PublicationItemDto.fromEntity(repository.save(toEntity(dto)));
    }

    public PublicationItemDto update(String id, PublicationItemDto dto) {
        PublicationItem existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publication not found."));
        String newId = dto.id().trim();
        if (!id.equals(newId)) {
            if (repository.existsById(newId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Publication already exists.");
            }
            repository.delete(existing);
            PublicationItem item = toEntity(dto);
            item.setId(newId);
            return PublicationItemDto.fromEntity(repository.save(item));
        }
        apply(existing, dto);
        return PublicationItemDto.fromEntity(repository.save(existing));
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publication not found.");
        }
        repository.deleteById(id);
    }

    private PublicationItem toEntity(PublicationItemDto dto) {
        PublicationItem item = new PublicationItem();
        item.setId(dto.id());
        apply(item, dto);
        return item;
    }

    private void apply(PublicationItem item, PublicationItemDto dto) {
        item.setTitle(dto.title());
        item.setExcerpt(dto.excerpt());
        item.setPublishDate(LocalDate.parse(dto.date()));
        item.setCategory(dto.category());
        item.setReadTime(dto.readTime());
        item.setAuthor(dto.author());
        item.setFeatured(dto.featured());
        item.setImageUrl(dto.imageUrl());
        item.setLink(dto.link());
        item.setSortOrder(dto.sortOrder());
        item.setActive(dto.active());
    }
}
