package org.ntnu.backend.medalion.dto;
                                                                                        
import lombok.AllArgsConstructor;                                                      
import lombok.Builder;                                                                 
import lombok.Data;                                                                    
import lombok.NoArgsConstructor;                                                                                                                                       
import java.time.LocalDateTime;                                                        
import java.util.UUID;                                                                 
                                                                                        
@Data                                                                                  
@Builder                                                                               
@NoArgsConstructor                                                                     
@AllArgsConstructor                                                                    
public class MedalionResponse {
    private UUID id;           
    private String name;
    private String description;
    private LocalDateTime earnedAt;
    private String imageUrl;
}                                  
                          
  