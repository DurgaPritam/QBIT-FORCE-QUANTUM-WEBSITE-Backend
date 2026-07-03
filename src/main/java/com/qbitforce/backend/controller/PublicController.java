package com.qbitforce.backend.controller;

import com.qbitforce.backend.dto.ApiMessage;
import com.qbitforce.backend.dto.ContactRequest;
import com.qbitforce.backend.dto.GalleryItemDto;
import com.qbitforce.backend.dto.PressMediaItemDto;
import com.qbitforce.backend.dto.PublicationItemDto;
import com.qbitforce.backend.dto.VideoItemDto;
import com.qbitforce.backend.service.ContactService;
import com.qbitforce.backend.service.GalleryService;
import com.qbitforce.backend.service.PressService;
import com.qbitforce.backend.service.PublicationService;
import com.qbitforce.backend.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final ContactService contactService;
    private final GalleryService galleryService;
    private final VideoService videoService;
    private final PublicationService publicationService;
    private final PressService pressService;

    public PublicController(
            ContactService contactService,
            GalleryService galleryService,
            VideoService videoService,
            PublicationService publicationService,
            PressService pressService) {
        this.contactService = contactService;
        this.galleryService = galleryService;
        this.videoService = videoService;
        this.publicationService = publicationService;
        this.pressService = pressService;
    }

    @PostMapping("/contact")
    public ApiMessage submitContact(@Valid @RequestBody ContactRequest request, HttpServletRequest httpRequest) {
        contactService.submit(request, resolveClientIp(httpRequest));
        return new ApiMessage("Thank you. Your enquiry has been received.");
    }

    @GetMapping("/gallery")
    public List<GalleryItemDto> gallery() {
        return galleryService.listPublic();
    }

    @GetMapping("/videos")
    public List<VideoItemDto> videos() {
        return videoService.listPublic();
    }

    @GetMapping("/publications")
    public List<PublicationItemDto> publications() {
        return publicationService.listPublic();
    }

    @GetMapping("/press")
    public List<PressMediaItemDto> press() {
        return pressService.listPublic();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
