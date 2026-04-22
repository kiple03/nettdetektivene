package org.ntnu.backend.medalion.dto;
                                                                                         
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;                                         
import lombok.Data;                                                                                                           
import java.util.UUID;
                    
@Data
public class AwardMedalionRequest {
                                    
    @NotNull                                                                           
    private UUID userId;
                                                                                        
    @NotBlank   
    private String name;
                        
    @NotBlank
    private String description;
}     