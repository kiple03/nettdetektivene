package org.ntnu.backend.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import org.ntnu.backend.filehandling.ContentRead;
import org.ntnu.backend.filehandling.ContentWrite;


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

    @Transient
    public List<Option> getOptions() {
        return ContentRead.read(this.optionContent);
    }

    public void setOptions(List<Option> options) {
        this.optionContent = ContentWrite.write(options);
    }
}