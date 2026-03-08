package com.campusops.booking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // e.g. "Lecture Hall", "Camera"

    @Column(nullable = false)
    private String location;

    private Integer capacity;

    private String status; // e.g. "ACTIVE", "OUT_OF_SERVICE"
}
