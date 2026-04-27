package org.ntnu.backend.game.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class ClassifyItemsOption implements Option {

  @NotNull
  private String key;

  @NotBlank
  private String text;


  @NotNull
  private List<String> labels;

  @NotBlank
  private String correctLabel;

  @NotBlank
  private String explanation;
}
