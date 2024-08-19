package org.cris6h16.apirestspringboot;

import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Services.UserServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;

@SpringBootApplication
public class ApiRestSpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiRestSpringBootApplication.class, args);
    }

    //    @Profile("prod") @Profile("!test") // does not work
    @Bean
    CommandLineRunner init(@org.springframework.lang.Nullable UserServiceImpl userService) { // I made it @Nullable and add null verification due to the fails of bean not fount in the Repository testing
        return args -> {
            if (userService == null) return;

            userService.create(
                    CreateUserDTO.builder()
                            .username("cris6h16")
                            .password("12345678")
                            .email("cristianmherrera21@gmail.com")
                            .build(),
                    ERole.ROLE_ADMIN
            );
        };
    }
}
