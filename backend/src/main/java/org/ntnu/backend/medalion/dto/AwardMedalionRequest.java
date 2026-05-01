package org.ntnu.backend.medalion.dto;
                                                                                         
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;                                         
import lombok.Data;                                                                                                           
import java.util.UUID;
                    
/**
 * Request data transfer object for awarding a medalion to a user.
 */
@Data
public class AwardMedalionRequest {
    /** ID of the user receiving the medalion. */
    @NotNull
    private UUID userId;

    /** Unique name identifying the medalion type. */
    @NotBlank
    private String name;

    /** Human-readable description shown to the user. */
    @NotBlank
    private String description;
}     