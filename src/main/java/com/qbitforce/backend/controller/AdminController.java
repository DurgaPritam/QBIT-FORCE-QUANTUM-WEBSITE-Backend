package com.qbitforce.backend.controller;

import com.qbitforce.backend.dto.AdminStatsDto;
import com.qbitforce.backend.dto.ChangePasswordRequest;
import com.qbitforce.backend.dto.ContactSubmissionDto;
import com.qbitforce.backend.dto.GalleryItemDto;
import com.qbitforce.backend.dto.PressMediaItemDto;
import com.qbitforce.backend.dto.PublicationItemDto;
import com.qbitforce.backend.dto.UploadResponse;
import com.qbitforce.backend.dto.VideoItemDto;
import com.qbitforce.backend.entity.ContactSubmission;
import com.qbitforce.backend.repository.ContactSubmissionRepository;
import com.qbitforce.backend.repository.GalleryItemRepository;
import com.qbitforce.backend.repository.PressMediaItemRepository;
import com.qbitforce.backend.repository.PublicationItemRepository;
import com.qbitforce.backend.repository.VideoItemRepository;
import com.qbitforce.backend.service.CloudinaryService;
import com.qbitforce.backend.service.GalleryService;
import com.qbitforce.backend.service.PasswordService;
import com.qbitforce.backend.service.PressService;
import com.qbitforce.backend.service.PublicationService;
import com.qbitforce.backend.service.VideoService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final GalleryService galleryService;
    private final VideoService videoService;
    private final PublicationService publicationService;
    private final PressService pressService;
    private final ContactSubmissionRepository contactSubmissionRepository;
    private final PasswordService passwordService;
    private final CloudinaryService cloudinaryService;
    private final GalleryItemRepository galleryItemRepository;
    private final VideoItemRepository videoItemRepository;
    private final PublicationItemRepository publicationItemRepository;
    private final PressMediaItemRepository pressMediaItemRepository;

    public AdminController(
            GalleryService galleryService,
            VideoService videoService,
            PublicationService publicationService,
            PressService pressService,
            ContactSubmissionRepository contactSubmissionRepository,
            PasswordService passwordService,
            CloudinaryService cloudinaryService,
            GalleryItemRepository galleryItemRepository,
            VideoItemRepository videoItemRepository,
            PublicationItemRepository publicationItemRepository,
            PressMediaItemRepository pressMediaItemRepository) {
        this.galleryService = galleryService;
        this.videoService = videoService;
        this.publicationService = publicationService;
        this.pressService = pressService;
        this.contactSubmissionRepository = contactSubmissionRepository;
        this.passwordService = passwordService;
        this.cloudinaryService = cloudinaryService;
        this.galleryItemRepository = galleryItemRepository;
        this.videoItemRepository = videoItemRepository;
        this.publicationItemRepository = publicationItemRepository;
        this.pressMediaItemRepository = pressMediaItemRepository;
    }

    @GetMapping("/stats")
    public AdminStatsDto stats() {
        return new AdminStatsDto(
                galleryItemRepository.count(),
                videoItemRepository.count(),
                publicationItemRepository.count(),
                pressMediaItemRepository.count(),
                contactSubmissionRepository.count(),
                contactSubmissionRepository.countByReadFalse());
    }

    @PostMapping("/upload")
    public UploadResponse upload(@RequestParam("file") MultipartFile file) {
        return cloudinaryService.uploadImage(file);
    }

    @PostMapping("/change-password")
    public com.qbitforce.backend.dto.ApiMessage changePassword(
            Principal principal, @Valid @RequestBody ChangePasswordRequest request) {
        passwordService.changePassword(principal.getName(), request);
        return new com.qbitforce.backend.dto.ApiMessage("Password updated successfully.");
    }

    @GetMapping("/contacts")
    public List<ContactSubmissionDto> contacts() {
        return contactSubmissionRepository.findAll().stream()
                .sorted(Comparator.comparing(c -> c.getCreatedAt(), Comparator.reverseOrder()))
                .map(ContactSubmissionDto::fromEntity)
                .toList();
    }

    @PatchMapping("/contacts/{id}/read")
    public ContactSubmissionDto markContactRead(
            @PathVariable Long id, @RequestParam(name = "read", defaultValue = "true") boolean read) {
        ContactSubmission submission = contactSubmissionRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found."));
        submission.setRead(read);
        return ContactSubmissionDto.fromEntity(contactSubmissionRepository.save(submission));
    }

    @DeleteMapping("/contacts/{id}")
    public void deleteContact(@PathVariable Long id) {
        if (!contactSubmissionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found.");
        }
        contactSubmissionRepository.deleteById(id);
    }

    @GetMapping("/gallery")
    public List<GalleryItemDto> gallery() {
        return galleryService.listAll();
    }

    @PostMapping("/gallery")
    public GalleryItemDto createGallery(@Valid @RequestBody GalleryItemDto dto) {
        return galleryService.create(dto);
    }

    @PutMapping("/gallery/{id}")
    public GalleryItemDto updateGallery(@PathVariable String id, @Valid @RequestBody GalleryItemDto dto) {
        return galleryService.update(id, dto);
    }

    @DeleteMapping("/gallery/{id}")
    public void deleteGallery(@PathVariable String id) {
        galleryService.delete(id);
    }

    @GetMapping("/videos")
    public List<VideoItemDto> videos() {
        return videoService.listAll();
    }

    @PostMapping("/videos")
    public VideoItemDto createVideo(@Valid @RequestBody VideoItemDto dto) {
        return videoService.create(dto);
    }

    @PutMapping("/videos/{id}")
    public VideoItemDto updateVideo(@PathVariable String id, @Valid @RequestBody VideoItemDto dto) {
        return videoService.update(id, dto);
    }

    @DeleteMapping("/videos/{id}")
    public void deleteVideo(@PathVariable String id) {
        videoService.delete(id);
    }

    @GetMapping("/publications")
    public List<PublicationItemDto> publications() {
        return publicationService.listAll();
    }

    @PostMapping("/publications")
    public PublicationItemDto createPublication(@Valid @RequestBody PublicationItemDto dto) {
        return publicationService.create(dto);
    }

    @PutMapping("/publications/{id}")
    public PublicationItemDto updatePublication(@PathVariable String id, @Valid @RequestBody PublicationItemDto dto) {
        return publicationService.update(id, dto);
    }

    @DeleteMapping("/publications/{id}")
    public void deletePublication(@PathVariable String id) {
        publicationService.delete(id);
    }

    @GetMapping("/press")
    public List<PressMediaItemDto> press() {
        return pressService.listAll();
    }

    @PostMapping("/press")
    public PressMediaItemDto createPress(@Valid @RequestBody PressMediaItemDto dto) {
        return pressService.create(dto);
    }

    @PutMapping("/press/{id}")
    public PressMediaItemDto updatePress(@PathVariable String id, @Valid @RequestBody PressMediaItemDto dto) {
        return pressService.update(id, dto);
    }

    @DeleteMapping("/press/{id}")
    public void deletePress(@PathVariable String id) {
        pressService.delete(id);
    }
}
