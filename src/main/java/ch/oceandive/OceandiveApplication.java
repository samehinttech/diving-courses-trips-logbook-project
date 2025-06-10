package ch.oceandive;

import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "ch.oceandive.controller",
    "ch.oceandive.controller.web",
    "ch.oceandive.controller.rest",
    "ch.oceandive.model",
    "ch.oceandive.security",
    "ch.oceandive.exceptionHandler",
    "ch.oceandive.validation",
    "ch.oceandive.service",
    "ch.oceandive.config",
    "ch.oceandive.repository"
})
public class OceandiveApplication {

  public static void main(String[] args) {
    ApplicationContext ctx = SpringApplication.run(OceandiveApplication.class, args);

    // Print all controller beans
    System.out.println("\n=== Registered Controllers ===");
    String[] beanNames = ctx.getBeanDefinitionNames();
    Arrays.stream(beanNames)
        .filter(name -> name.toLowerCase().contains("controller"))
        .sorted()
        .forEach(name -> System.out.println("Controller Bean: " + name));
    System.out.println("=== End Controllers ===\n");
  }
}