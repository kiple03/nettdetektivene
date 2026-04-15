package org.ntnu.backend.level.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import org.ntnu.backend.level.model.enums.levelType;

@Entity
@Table(name = "levels")
@Getter @Setter
public class  Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private levelType levelType;

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL)
    private List<Question> questions;
}