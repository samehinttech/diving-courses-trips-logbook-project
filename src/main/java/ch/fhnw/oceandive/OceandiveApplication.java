package ch.fhnw.oceandive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class OceandiveApplication {

  public static void main(String[] args) {
    // Load environment variables from .env file


    SpringApplication.run(OceandiveApplication.class, args);
  }
}
