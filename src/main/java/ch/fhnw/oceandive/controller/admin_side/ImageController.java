package ch.fhnw.oceandive.controller.admin_side;

import ch.fhnw.oceandive.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling course image uploads by admin.
 */
@RestController
@RequestMapping("/api/admin/images")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * POST /api/admin/images/upload: Upload a new image for a course.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = imageService.saveImage(file);
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * DELETE /api/admin/images : Delete an image.
     */
    @DeleteMapping("/delete-image")
    public ResponseEntity<Void> deleteImage(@RequestParam("imageUrl") String imageUrl) {
        boolean deleted = imageService.deleteImage(imageUrl);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
