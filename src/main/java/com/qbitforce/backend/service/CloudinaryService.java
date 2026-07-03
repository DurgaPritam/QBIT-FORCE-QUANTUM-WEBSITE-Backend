package com.qbitforce.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbitforce.backend.config.CloudinaryProperties;
import com.qbitforce.backend.dto.UploadResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    private final CloudinaryProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public CloudinaryService(CloudinaryProperties properties) {
        this.properties = properties;
    }

    public UploadResponse uploadImage(MultipartFile file) {
        if (!properties.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cloudinary is not configured.");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required.");
        }

        try {
            long timestamp = System.currentTimeMillis() / 1000L;
            String signature = sign("timestamp=" + timestamp);
            String boundary = "----QbitForceBoundary" + timestamp;
            byte[] body = buildMultipartBody(boundary, file, timestamp, signature);

            String uploadUrl =
                    "https://api.cloudinary.com/v1_1/" + properties.getCloudName() + "/image/upload";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode json = objectMapper.readTree(response.body());
            if (response.statusCode() >= 400) {
                String cloudinaryMessage = json.path("error").path("message").asText("");
                log.warn("Cloudinary upload rejected ({}): {}", response.statusCode(), cloudinaryMessage);
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        cloudinaryMessage.isBlank() ? "Cloudinary upload failed." : cloudinaryMessage);
            }

            String secureUrl = json.path("secure_url").asText();
            String publicId = json.path("public_id").asText();
            if (secureUrl.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary upload failed.");
            }
            return new UploadResponse(secureUrl, publicId);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary upload interrupted.");
        } catch (Exception ex) {
            log.error("Cloudinary upload error", ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Cloudinary upload failed: " + ex.getMessage());
        }
    }

    private byte[] buildMultipartBody(String boundary, MultipartFile file, long timestamp, String signature)
            throws IOException {
        String crlf = "\r\n";
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeTextField(out, boundary, "api_key", properties.getApiKey(), crlf);
        writeTextField(out, boundary, "timestamp", String.valueOf(timestamp), crlf);
        writeTextField(out, boundary, "signature", signature, crlf);

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.jpg";
        String contentType =
                file.getContentType() != null && !file.getContentType().isBlank()
                        ? file.getContentType()
                        : "application/octet-stream";

        out.write(("--" + boundary + crlf).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"" + crlf)
                .getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + contentType + crlf + crlf).getBytes(StandardCharsets.UTF_8));
        out.write(file.getBytes());
        out.write((crlf + "--" + boundary + "--" + crlf).getBytes(StandardCharsets.UTF_8));

        return out.toByteArray();
    }

    private void writeTextField(
            ByteArrayOutputStream out, String boundary, String name, String value, String crlf)
            throws IOException {
        out.write(("--" + boundary + crlf).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"" + crlf + crlf)
                .getBytes(StandardCharsets.UTF_8));
        out.write(value.getBytes(StandardCharsets.UTF_8));
        out.write(crlf.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String payload) throws NoSuchAlgorithmException {
        String toSign = payload + properties.getApiSecret();
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(toSign.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
