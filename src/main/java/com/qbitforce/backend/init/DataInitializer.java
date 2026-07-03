package com.qbitforce.backend.init;

import com.qbitforce.backend.config.AdminProperties;
import com.qbitforce.backend.entity.*;
import com.qbitforce.backend.repository.*;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AdminUserRepository adminUserRepository;
    private final GalleryItemRepository galleryItemRepository;
    private final VideoItemRepository videoItemRepository;
    private final PublicationItemRepository publicationItemRepository;
    private final PressMediaItemRepository pressMediaItemRepository;
    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            AdminUserRepository adminUserRepository,
            GalleryItemRepository galleryItemRepository,
            VideoItemRepository videoItemRepository,
            PublicationItemRepository publicationItemRepository,
            PressMediaItemRepository pressMediaItemRepository,
            AdminProperties adminProperties,
            PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.galleryItemRepository = galleryItemRepository;
        this.videoItemRepository = videoItemRepository;
        this.publicationItemRepository = publicationItemRepository;
        this.pressMediaItemRepository = pressMediaItemRepository;
        this.adminProperties = adminProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        if (galleryItemRepository.count() == 0) {
            seedGallery();
        } else {
            syncGalleryDefaults();
        }
        if (videoItemRepository.count() == 0) {
            seedVideos();
        }
        if (publicationItemRepository.count() == 0) {
            seedPublications();
        }
        if (pressMediaItemRepository.count() == 0) {
            seedPress();
        }
    }

    private void seedAdmin() {
        if (adminProperties.getPassword() == null || adminProperties.getPassword().isBlank()) {
            throw new IllegalStateException("ADMIN_PASSWORD is required in .env to create the initial admin user.");
        }

        String email = adminProperties.getEmail().trim().toLowerCase();

        adminUserRepository.findAll().forEach(admin -> {
            if (admin.getEmail() == null || !email.equalsIgnoreCase(admin.getEmail())) {
                adminUserRepository.delete(admin);
            }
        });

        if (adminUserRepository.findByEmailIgnoreCase(email).isEmpty()) {
            AdminUser admin = new AdminUser();
            admin.setUsername(email);
            admin.setEmail(email);
            admin.setPasswordHash(passwordEncoder.encode(adminProperties.getPassword()));
            admin.setRole("ROLE_ADMIN");
            adminUserRepository.save(admin);
            log.info("Admin user created: {}", email);
        }
    }

    private void seedGallery() {
        saveGallery(
                "gallery-quantum-frontier-launch",
                "India's First Open Access Quantum Frontier",
                "Launch of the Amaravati Quantum Reference Facilities at SRM University Amaravati — built in Amaravati, open to India, for the world.",
                "events",
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1780935950/Copy_of_IMG_20260414_161438_zed6bs.jpg",
                1);
        saveGallery(
                "gallery-leadership-inauguration",
                "Quantum Leadership at Amaravati",
                "Dignitaries and partners at the inauguration of India's open-access quantum frontier.",
                "events",
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1780935959/Copy_of_IMG_20260414_144515_wl0abz.jpg",
                2);
        saveGallery(
                "company-1",
                "Amaravati Quantum Valley",
                "Building indigenous quantum hardware at scale in Andhra Pradesh.",
                "facility",
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1780937130/WhatsApp_Image_2026-06-07_at_6.53.46_PM_vshegz.jpg",
                3);
        saveGallery(
                "gallery-srm-leadership-visit",
                "Leadership Visit at SRM University AP",
                "Reviewing quantum and computing innovations with students and faculty at SRM University AP.",
                "events",
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1782461778/WhatsApp_Image_2026-06-25_at_10.20.07_PM_lxrjmn.jpg",
                4);
        log.info("Seeded gallery items");
    }

    private void syncGalleryDefaults() {
        upsertGalleryIfMissing(
                "gallery-quantum-frontier-launch",
                "India's First Open Access Quantum Frontier",
                "Launch of the Amaravati Quantum Reference Facilities at SRM University Amaravati — built in Amaravati, open to India, for the world.",
                "events",
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1780935950/Copy_of_IMG_20260414_161438_zed6bs.jpg",
                1);
        upsertGalleryIfMissing(
                "gallery-leadership-inauguration",
                "Quantum Leadership at Amaravati",
                "Dignitaries and partners at the inauguration of India's open-access quantum frontier.",
                "events",
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1780935959/Copy_of_IMG_20260414_144515_wl0abz.jpg",
                2);
        upsertGalleryIfMissing(
                "company-1",
                "Amaravati Quantum Valley",
                "Building indigenous quantum hardware at scale in Andhra Pradesh.",
                "facility",
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1780937130/WhatsApp_Image_2026-06-07_at_6.53.46_PM_vshegz.jpg",
                3);
        upsertGalleryIfMissing(
                "gallery-srm-leadership-visit",
                "Leadership Visit at SRM University AP",
                "Reviewing quantum and computing innovations with students and faculty at SRM University AP.",
                "events",
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1782461778/WhatsApp_Image_2026-06-25_at_10.20.07_PM_lxrjmn.jpg",
                4);
    }

    private void upsertGalleryIfMissing(
            String id, String title, String caption, String category, String imageUrl, int order) {
        if (!galleryItemRepository.existsById(id)) {
            saveGallery(id, title, caption, category, imageUrl, order);
        }
    }

    private void saveGallery(String id, String title, String caption, String category, String imageUrl, int order) {
        GalleryItem item = new GalleryItem();
        item.setId(id);
        item.setTitle(title);
        item.setCaption(caption);
        item.setCategory(category);
        item.setImageUrl(imageUrl);
        item.setSortOrder(order);
        galleryItemRepository.save(item);
    }

    private void seedVideos() {
        saveVideo("qf-video-1", "Qbit Force — Facility Walkthrough",
                "Walkthrough of our Amaravati quantum hardware facility: cryogenic assembly, control systems, and open-access platforms.",
                "facility",
                "https://res.cloudinary.com/dps46p3m8/video/upload/v1780935951/WhatsApp_Video_2026-06-07_at_7.40.37_PM_ij7kaw.mp4",
                null,
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1780935950/Copy_of_IMG_20260414_161438_zed6bs.jpg", 1);
        saveVideo("qf-video-2", "Qbit Force — Lab & Operations",
                "Inside our quantum hardware lab: manufacturing, assembly, and day-to-day operations at Qbit Force.",
                "facility",
                "https://res.cloudinary.com/dps46p3m8/video/upload/v1780935956/WhatsApp_Video_2026-06-07_at_7.52.46_PM_nmic6w.mp4",
                null,
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1780935960/Copy_of_IMG_20260414_161532_l2o2mc.jpg", 2);
        saveVideo("qf-video-youtube-1", "Qbit Force — Featured Coverage",
                "Watch our latest featured video coverage on quantum hardware development in India.",
                "events", null, "pIXXVpTbKh4", null, 3);
        saveVideo("qf-video-youtube-2", "Qbit Force — Live Session",
                "Live stream from Qbit Force — quantum computing updates and facility highlights.",
                "events", null, "0EvUWqsqC6g", null, 4);
        log.info("Seeded videos");
    }

    private void saveVideo(String id, String title, String description, String category, String src,
            String youtubeId, String thumbnail, int order) {
        VideoItem item = new VideoItem();
        item.setId(id);
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        item.setSrc(src);
        item.setYoutubeId(youtubeId);
        item.setThumbnail(thumbnail != null ? thumbnail :
                (youtubeId != null ? "https://img.youtube.com/vi/" + youtubeId + "/hqdefault.jpg" : null));
        item.setSortOrder(order);
        videoItemRepository.save(item);
    }

    private void seedPublications() {
        savePublication("a5", "Amaravati Quantum Facility Reaches 4 Kelvin Milestone",
                "The Amaravati Quantum Reference Facility achieved a major milestone as its indigenous dilution refrigerator successfully reached 4 Kelvin (-269°C), strengthening India's capabilities in cryogenic engineering and advancing the National Quantum Mission. Made in Amaravati, for the World.",
                LocalDate.of(2026, 6, 21), "publication", "Andhra Pradesh Economic Development Board", true,
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1782234524/1782038591880_w5sr3g.jpg",
                "https://www.linkedin.com/posts/quantumcapitalamaravati-nationalquantummission-share-7474411595910103040-KSox/", 1);
        savePublication("a6", "Amaravati at World Cities Summit — Building Cities That Love People Back",
                "Andhra Pradesh Economic Development Board highlights Amaravati's future cities vision at the World Cities Summit, showcasing people-centric urban development alongside the state's quantum innovation agenda under Choose Speed, Choose AP.",
                LocalDate.of(2026, 6, 19), "publication", "Andhra Pradesh Economic Development Board", true,
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1782234882/Screenshot_2026-06-23_224408_zcfbxt.png",
                "https://www.linkedin.com/posts/apedb_worldcitiessummit-amaravati-futurecities-activity-7473334024397090816-hKlI/", 2);
        savePublication("a1", "Amaravati Quantum Valley Announces Quantum Computers Initiative",
                "Highlights from the Amaravati Quantum Valley announcement, focusing on the establishment of quantum computing infrastructure and the growth of India's quantum ecosystem.",
                LocalDate.of(2026, 2, 7), "press", null, true,
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1781023863/DAS_5340_g7me7u.jpg",
                "https://www.linkedin.com/posts/amaravati-quantum-valley-quantum-computers-share-7465660005392945152-SieP/", 3);
        savePublication("a2", "Principal Scientific Adviser Highlights Quantum Innovation",
                "A discussion on India's quantum technology roadmap and the role of indigenous quantum computing initiatives in advancing national capabilities.",
                LocalDate.of(2026, 2, 7), "publication", null, true,
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1781023913/Screenshot_2026-06-09_222110_ym7lbg.png",
                "https://www.linkedin.com/posts/principal-scientific-adviser-to-the-government-ugcPost-7449851435250372609-7-w2/", 4);
        savePublication("a3", "Andhra Pradesh Emerging as a Quantum Technology Hub",
                "Industry leaders and innovators discuss the opportunities being created through Andhra Pradesh's investment in quantum technologies and advanced manufacturing.",
                LocalDate.of(2026, 1, 15), "insight", null, false,
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1781024008/1776170355903_jlqwvl.jpg",
                "https://www.linkedin.com/posts/india-andhrapradesh-deccanfounders-share-7449798429800652800-2qJv/", 5);
        savePublication("a4", "The India Way: Building a Sustainable Quantum Ecosystem",
                "Perspectives on strengthening India's position in quantum computing through innovation, collaboration, and indigenous technology development.",
                LocalDate.of(2025, 12, 10), "publication", null, false,
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1781024008/1780987927822_nfgvuk.jpg",
                "https://www.linkedin.com/posts/bharatstrategicapexinstitute-bsai-theindiaway-ugcPost-7470006850126741504-P-St/", 6);
        log.info("Seeded publications");
    }

    private void savePublication(String id, String title, String excerpt, LocalDate date, String category,
            String author, boolean featured, String imageUrl, String link, int order) {
        PublicationItem item = new PublicationItem();
        item.setId(id);
        item.setTitle(title);
        item.setExcerpt(excerpt);
        item.setPublishDate(date);
        item.setCategory(category);
        item.setAuthor(author);
        item.setFeatured(featured);
        item.setImageUrl(imageUrl);
        item.setLink(link);
        item.setSortOrder(order);
        publicationItemRepository.save(item);
    }

    private void seedPress() {
        savePress("news-4", "Quantum computing achieves an indigenous milestone",
                "Business Standard reports that Amaravati Quantum Valley's indigenous dilution refrigerator reached 4 Kelvin at the Quantum Reference Facility in Medha Towers — one of the coldest temperatures achieved in an Indian research facility, with more than 80% domestically sourced components. Qbit Force partnered with AQV and Qubitech to map India's quantum hardware supply chain.",
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1782234524/1782054463833_ts5zfk.jpg", 1);
        savePress("news-1", "SiliconIndia Exclusive Coverage",
                "The Big Milestone in Amaravati That Unlocks India's Quantum Potential – featuring the launch of India's first open-access quantum facility.",
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1780937129/WhatsApp_Image_2026-06-07_at_6.53.49_PM_1_wmrujs.jpg", 2);
        savePress("news-2", "Business India – Aiming for a Quantum Leap",
                "Business India highlights Andhra Pradesh's quantum push and the inauguration of the Amaravati Quantum Reference Facility.",
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1780937129/WhatsApp_Image_2026-06-07_at_6.53.49_PM_ujd0wk.jpg", 3);
        savePress("news-3", "Business India Magazine Feature",
                "Speed and scale define Andhra Pradesh's quantum ambitions, showcasing Amaravati's emergence as a national quantum innovation hub.",
                "https://res.cloudinary.com/dps46p3m8/image/upload/v1780937129/WhatsApp_Image_2026-06-07_at_6.53.48_PM_2_ymefan.jpg", 4);
        log.info("Seeded press media items");
    }

    private void savePress(String id, String title, String caption, String imageUrl, int order) {
        PressMediaItem item = new PressMediaItem();
        item.setId(id);
        item.setTitle(title);
        item.setCaption(caption);
        item.setImageUrl(imageUrl);
        item.setSortOrder(order);
        pressMediaItemRepository.save(item);
    }
}
