package org.ntnu.backend.game.model;

import jakarta.persistence.*;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "questions")
@Getter @Setter
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String questionText;

    @Column(columnDefinition = "JSON", nullable = false)
    private String optionContent;

    @ManyToOne
    @JoinColumn(name = "level_id")
    private Level level;
}