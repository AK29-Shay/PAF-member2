package com.campusops.booking.controller;

import com.campusops.booking.model.Resource;
import com.campusops.booking.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResourceController {

    private final ResourceRepository resourceRepository;

    @GetMapping
    public ResponseEntity<List<Resource>> getAllResources() {
        return ResponseEntity.ok(resourceRepository.findAll());
    }
}
