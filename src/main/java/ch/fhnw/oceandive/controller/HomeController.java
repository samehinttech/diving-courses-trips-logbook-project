package ch.fhnw.oceandive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling the root path of the application.
 */
@Controller
public class HomeController {

    /**
     * Handles the API info endpoint.
     * @return A JSON response with basic API information
     */
    @GetMapping("/api")
    @ResponseBody
    public Map<String, Object> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Welcome to Ocean Dive API");
        response.put("version", "1.0");
        response.put("endpoints", new String[] {
            "/api/trips", 
            "/api/courses", 
            "/api/auth/user/register", 
            "/api/auth/user/login",
            "/api/about",
            "/api/contact",
            "/api/privacy-policy",
            "/api/terms-and-conditions"
        });
        return response;
    }
}
