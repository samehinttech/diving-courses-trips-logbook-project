package ch.fhnw.oceandive.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for static content pages.
 */
@RestController
@RequestMapping("/api")
public class ContentController {


    @GetMapping("/about")
    public ResponseEntity<Map<String, Object>> getAboutInfo() {
        Map<String, Object> aboutInfo = new HashMap<>();
        aboutInfo.put("companyName", "Ocean Dive");
        return ResponseEntity.ok(aboutInfo);
    }

    @GetMapping("/contact")
    public ResponseEntity<Map<String, Object>> getContactInfo() {
        Map<String, Object> contactInfo = new HashMap<>();
        contactInfo.put("address", "000, Seaside Strasse 8, RS 0000");

        return ResponseEntity.ok(contactInfo);
    }

    @GetMapping("/privacy-policy")
    public ResponseEntity<Map<String, Object>> getPrivacyPolicy() {
        Map<String, Object> privacyPolicy = new HashMap<>();
        privacyPolicy.put("title", "Privacy Policy");
        return ResponseEntity.ok(privacyPolicy);
    }

    @GetMapping("/terms-conditions")
    public ResponseEntity<Map<String, Object>> getTermsAndConditions() {
        Map<String, Object> termsAndConditions = new HashMap<>();
        termsAndConditions.put("title", "Terms and Conditions");
        return ResponseEntity.ok(termsAndConditions);
    }
}
