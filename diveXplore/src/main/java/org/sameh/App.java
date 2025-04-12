package org.sameh;

/*
This is the main class for the Spring Boot application.
It contains the main method which is the entry point of the application.
It uses SpringApplication.run() to launch the application.
*/

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
public class App 
{
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

    