package ch.fhnw.oceandive;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class OceandiveApplication {

  public static void main(String[] args) {
    // Load environment variables from .env file
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    SpringApplication.run(OceandiveApplication.class, args);
  }
}
