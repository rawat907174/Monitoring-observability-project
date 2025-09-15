package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class DemoApplication {
  private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

  @GetMapping("/hello")
  public String hello() {
    log.info("hello endpoint called");
    return "hello world";
  }

  @GetMapping("/error500")
  public String boom() {
    log.error("simulated error endpoint");
    throw new RuntimeException("simulated failure");
  }

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }
}
