package ch.oceandive;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OceandiveApplication {

  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure()
        .directory("./") // path to your .env file
        .ignoreIfMissing()
        .load();
    dotenv.entries().forEach(entry ->
        System.setProperty(entry.getKey(), entry.getValue())
    );
    SpringApplication.run(OceandiveApplication.class, args);
  }
}