package ch.fhnw.oceandive;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class OceandiveApplication {

  public static void main(String[] args) {
    // Load environment variables from .env file
    Dotenv dotenv = Dotenv.configure()
        .ignoreIfMissing()
        .load();
    // Set JWT environment variables for Spring to use
    System.setProperty("JWT_SECRET_KEY", dotenv.get("JWT_SECRET_KEY"));
    System.setProperty("JWT_ISSUER", dotenv.get("JWT_ISSUER"));
    System.setProperty("JWT_AUDIENCE", dotenv.get("JWT_AUDIENCE"));
    System.setProperty("JWT_EXPIRATION_TIME", dotenv.get("JWT_EXPIRATION_TIME"));
    System.setProperty("JWT_SIGNING_ALGORITHM", dotenv.get("JWT_SIGNING_ALGORITHM"));

    SpringApplication.run(OceandiveApplication.class, args);
  }
}
