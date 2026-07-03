package com.qbitforce.backend.service;

import com.qbitforce.backend.dto.VideoItemDto;
import com.qbitforce.backend.entity.VideoItem;
import com.qbitforce.backend.repository.VideoItemRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VideoService {

    private final VideoItemRepository repository;

    public VideoService(VideoItemRepository repository) {
        this.repository = repository;
    }

    public List<VideoItemDto> listPublic() {
        return repository.findByActiveTrueOrderBySortOrderAscCreatedAtDesc().stream()
                .map(VideoItemDto::fromEntity)
                .toList();
    }

    public List<VideoItemDto> listAll() {
        return repository.findAll().stream().map(VideoItemDto::fromEntity).toList();
    }

    public VideoItemDto create(VideoItemDto dto) {
        if (repository.existsById(dto.id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Video already exists.");
        }
        return VideoItemDto.fromEntity(repository.save(toEntity(dto)));
    }

    public VideoItemDto update(String id, VideoItemDto dto) {
        VideoItem existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found."));
        String newId = dto.id().trim();
        if (!id.equals(newId)) {
            if (repository.existsById(newId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Video already exists.");
            }
            repository.delete(existing);
            VideoItem item = toEntity(dto);
            item.setId(newId);
            return VideoItemDto.fromEntity(repository.save(item));
        }
        apply(existing, dto);
        return VideoItemDto.fromEntity(repository.save(existing));
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found.");
        }
        repository.deleteById(id);
    }

    private VideoItem toEntity(VideoItemDto dto) {
        VideoItem item = new VideoItem();
        item.setId(dto.id());
        apply(item, dto);
        return item;
    }

    private void apply(VideoItem item, VideoItemDto dto) {
        item.setTitle(dto.title());
        item.setDescription(dto.description());
        item.setDuration(dto.duration());
        item.setCategory(dto.category());
        item.setSrc(dto.src());
        item.setYoutubeId(dto.youtubeId());
        item.setThumbnail(dto.thumbnail());
        item.setSortOrder(dto.sortOrder());
        item.setActive(dto.active());
    }
}
