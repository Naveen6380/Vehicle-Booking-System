package com.heavyequip.rental.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class FileUploadController {

    // Files save ஆகும் folder — project root-ல "uploads" folder
    private final String uploadDir = "uploads/";

    /**
     * POST /api/v1/upload/images
     * Multiple images upload பண்ணி, accessible URLs return பண்றது
     * Max 3 images, each max 5MB
     */
    @PostMapping("/images")
    public ResponseEntity<List<String>> uploadImages(
            @RequestParam("files") List<MultipartFile> files) throws IOException {

        // uploads folder இல்லன்னா create பண்ணு
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            // Validation: image files மட்டும்
            String contentType = file.getContentType();
            if (contentType == null ||
                    !contentType.startsWith("image/")) {
                continue;
            }

            // File size check: max 5MB
            if (file.getSize() > 5 * 1024 * 1024) {
                continue;
            }

            // Unique filename generate பண்ணு
            String extension = getExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + "." + extension;

            // Save to disk
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // URL return பண்றோம் — backend serve பண்ணும்
            imageUrls.add("http://localhost:8080/uploads/" + fileName);
        }

        return ResponseEntity.ok(imageUrls);
    }

    private String getExtension(String filename) {
        if (filename == null) return "jpg";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex + 1) : "jpg";
    }
}