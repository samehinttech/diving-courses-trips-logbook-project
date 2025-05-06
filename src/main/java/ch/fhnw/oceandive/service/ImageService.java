package ch.fhnw.oceandive.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service for handling image uploads for courses.
 */
@Service
public class ImageService {

    @Value("${app.upload.dir:${user.home}/oceandive/uploads}")
    private String uploadDir;
    
    private static final String IMAGE_BASE_URL = "/oceandive/images/";

    /**
     * Save an image file to the server and return its URL path.
     *
     * @param file the image file to save
     * @return the URL path to access the image
     * @throws IOException if an error occurs during saving
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot save empty file");
        }
        
        // Generate a unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        
        // Create directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Save the file to the server
        Path path = Paths.get(uploadDir, uniqueFilename);
        Files.write(path, file.getBytes());
        
        // Return the URL path to access the image
        return IMAGE_BASE_URL + uniqueFilename;
    }
    
    /**
     * Delete an image file from the server.
     *
     * @param imageUrl the URL of the image to delete
     * @return true if the image was deleted successfully, false otherwise
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public boolean deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }
        
        // Extract the filename from the URL
        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        
        // Delete the file from the server
        Path path = Paths.get(uploadDir, filename);
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }
}
