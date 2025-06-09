package ch.oceandive.deprecated;
/**

import java.security.SecureRandom;
import java.util.Base64;

public class JwtSecretGenerator {
  public static void main(String[] args) {
    // Generate 96 random bytes (Base64-encoded = 128 characters)
    byte[] randomBytes = new byte[96];
    new SecureRandom().nextBytes(randomBytes);
    String base64Secret = Base64.getEncoder().encodeToString(randomBytes);

    System.out.println("Generated JWT Secret (128-character Base64):");
    System.out.println(base64Secret);
  }
}

// This class is not used in the app, only for generating JWT secrets for testing purposes
 */
