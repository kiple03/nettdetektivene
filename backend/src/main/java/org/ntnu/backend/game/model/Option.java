package org.ntnu.backend.game.model;

/**
 * Sealed interface for options.
 * The root of the tagged onion for options. Include all options that are permitted.
 */
public sealed interface Option
permits MultipleChoiceOption {

}
