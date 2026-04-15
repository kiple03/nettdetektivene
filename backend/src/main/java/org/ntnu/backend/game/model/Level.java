package org.ntnu.backend.game.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import org.ntnu.backend.game.model.enums.levelType;

@Entity
@Table(name = "levels")
@Getter @Setter
public class  Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Enumerated(EnumType.STRING)
    private levelType levelType;
}