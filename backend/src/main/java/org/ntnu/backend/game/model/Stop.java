package org.ntnu.backend.game.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.ntnu.backend.game.repository.LevelRepository;


/**
 * A location on the game map. Stops are global, so they exist independently
 * of any classroom and are shared by all students.
 *
 * <p>Each stop contains multiple {@link Level}s accessed via
 * {@code LevelRepository.findByStopId()}. Students progress through
 * stops in {@link #displayOrder} sequence, unlocking the next stop
 * by completing all levels in the current one.
 *
 * @see Level
 */
@Entity
@Table(name = "stops")
@Getter
@Setter
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
