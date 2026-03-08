package com.campusops.booking;

import com.campusops.booking.model.Resource;
import com.campusops.booking.model.User;
import com.campusops.booking.repository.ResourceRepository;
import com.campusops.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder().name("Standard User").email("user@example.com").role("USER").build());
            userRepository.save(User.builder().name("Admin User").email("admin@example.com").role("ADMIN").build());
            System.out.println("Seeded test users.");
        }

        if (resourceRepository.count() == 0) {
            resourceRepository.save(Resource.builder().name("Lecture Hall A").type("Lecture Hall").capacity(100).location("Building 1").status("ACTIVE").build());
            resourceRepository.save(Resource.builder().name("Meeting Room 1").type("Meeting Room").capacity(10).location("Library").status("ACTIVE").build());
            resourceRepository.save(Resource.builder().name("Sony Camera A7").type("Equipment").capacity(1).location("Media Lab").status("ACTIVE").build());
            resourceRepository.save(Resource.builder().name("Broken Projector").type("Equipment").capacity(1).location("Store").status("OUT_OF_SERVICE").build());
            System.out.println("Seeded test resources.");
        }
    }
}
