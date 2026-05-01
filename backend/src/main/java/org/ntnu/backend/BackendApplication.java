package org.ntnu.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Entry point for the Spring Boot backend application. */
@SpringBootApplication
@EnableScheduling
public class BackendApplication {

  /**
   * Bootstraps the Spring application context and starts the embedded server.
   *
   * @param args command-line arguments passed to the application
   */
  public static void main(String[] args) {
    SpringApplication.run(BackendApplication.class, args);
  }

}
