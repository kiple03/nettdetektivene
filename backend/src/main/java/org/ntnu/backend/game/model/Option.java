package org.ntnu.backend.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Option {
    private int id;
    private String text;
    private String imageUrl;
    private boolean isCorrect;
    private String explanation;
}