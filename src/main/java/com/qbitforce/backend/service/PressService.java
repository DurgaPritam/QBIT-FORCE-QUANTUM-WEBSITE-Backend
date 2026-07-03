package com.qbitforce.backend.service;

import com.qbitforce.backend.dto.PressMediaItemDto;
import com.qbitforce.backend.entity.PressMediaItem;
import com.qbitforce.backend.repository.PressMediaItemRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PressService {

    private final PressMediaItemRepository repository;

    public PressService(PressMediaItemRepository repository) {
        this.repository = repository;
    }

    public List<PressMediaItemDto> listPublic() {
        return repository.findByActiveTrueOrderBySortOrderAscCreatedAtDesc().stream()
                .map(PressMediaItemDto::fromEntity)
                .toList();
    }

    public List<PressMediaItemDto> listAll() {
        return repository.findAll().stream().map(PressMediaItemDto::fromEntity).toList();
    }

    public PressMediaItemDto create(PressMediaItemDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Press item already exists.");
        }
        return PressMediaItemDto.fromEntity(repository.save(toEntity(dto)));
    }

    public PressMediaItemDto update(String id, PressMediaItemDto dto) {
        PressMediaItem existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Press item not found."));
        String newId = dto.id().trim();
        if (!id.equals(newId)) {
            if (repository.existsById(newId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Press item already exists.");
            }
            repository.delete(existing);
            PressMediaItem item = toEntity(dto);
            item.setId(newId);
            return PressMediaItemDto.fromEntity(repository.save(item));
        }
        apply(existing, dto);
        return PressMediaItemDto.fromEntity(repository.save(existing));
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Press item not found.");
        }
        repository.deleteById(id);
    }

    private PressMediaItem toEntity(PressMediaItemDto dto) {
        PressMediaItem item = new PressMediaItem();
        item.setId(dto.id());
        apply(item, dto);
        return item;
    }

    private void apply(PressMediaItem item, PressMediaItemDto dto) {
        item.setTitle(dto.title());
        item.setCaption(dto.caption());
        item.setCategory(dto.category());
        item.setImageUrl(dto.imageUrl());
        item.setSortOrder(dto.sortOrder());
        item.setActive(dto.active());
    }
}
