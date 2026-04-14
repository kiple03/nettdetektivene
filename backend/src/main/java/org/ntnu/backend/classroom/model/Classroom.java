package org.ntnu.backend.classroom.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "classrooms")
@Getter @Setter
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;
    private String description;

    @Column(unique = true, nullable = false, length = 8)
    private String joinCode;
}