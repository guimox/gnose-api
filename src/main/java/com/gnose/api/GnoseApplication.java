package com.gnose.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableAutoConfiguration
@RestController
public class GnoseApplication {

    @GetMapping
    public String applicationStatus() {
        return "Application is up and running, everything's okay";
    }

    public static void main(String[] args) {
        SpringApplication.run(GnoseApplication.class, args);
    }

}
