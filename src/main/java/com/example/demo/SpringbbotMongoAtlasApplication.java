package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class SpringbbotMongoAtlasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbbotMongoAtlasApplication.class, args);

        System.out.println(new BCryptPasswordEncoder().encode("userpass2"));

    }

}
