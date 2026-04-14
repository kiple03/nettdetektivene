package org.ntnu.backend.level.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "levels")
@Getter @Setter
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;
    private String description;

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL)
    private List<Question> questions;
}