package ch.fhnw.oceandive.controller;

import ch.fhnw.oceandive.model.DiveCertification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for dive certifications.
 */
@RestController
@RequestMapping("/api/certifications")
public class DiveCertificationController {

    /**
     * GET /api/certifications : Get all dive certifications.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAllCertifications() {
        List<Map<String, String>> certifications = Arrays.stream(DiveCertification.values())
                .map(cert -> Map.of(
                        "name", cert.name(),
                        "displayName", cert.getDisplayName()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(certifications);
    }

    /**
     * GET /api/certifications/{name} : Get a specific dive certification by name.
     */
    @GetMapping("/{name}")
    public ResponseEntity<?> getCertificationByName(@PathVariable String name) {
        try {
            DiveCertification certification = DiveCertification.valueOf(name.toUpperCase());
            Map<String, String> response = Map.of(
                    "name", certification.name(),
                    "displayName", certification.getDisplayName()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid certification name: " + name);
        }
    }

    /**
     * GET /api/certifications/check/{name} : Check if a certification name is valid.
     */
    @GetMapping("/check/{name}")
    public ResponseEntity<Map<String, Boolean>> checkCertificationExists(@PathVariable String name) {
        boolean exists = Arrays.stream(DiveCertification.values())
                .anyMatch(cert -> cert.name().equalsIgnoreCase(name));
        
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * GET /api/certifications/display/{displayName} : Get a certification by its display name.
     */
    @GetMapping("/display/{displayName}")
    public ResponseEntity<?> getCertificationByDisplayName(@PathVariable String displayName) {
        try {
            DiveCertification certification = DiveCertification.fromString(displayName);
            Map<String, String> response = Map.of(
                    "name", certification.name(),
                    "displayName", certification.getDisplayName()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}