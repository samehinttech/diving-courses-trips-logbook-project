package ch.oceandive.controller.rest;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health controller added while deployment on render to check the health of the application.
 */

@RestController
public class HealthController {

  @GetMapping("/api/health")
  public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("I am Still Alive Thank you Sameh!");

  }
}
