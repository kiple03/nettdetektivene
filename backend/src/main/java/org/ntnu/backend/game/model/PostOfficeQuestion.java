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
public class PostOfficeQuestion {

  @NotNull
  private Email email;
  @NotNull
  private List<PostOfficeOption> options;
  @NotNull
  private List<String> correctSuspicious;
  @NotBlank
  private String recommendedAction;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Email {
    @NotBlank
    private String senderName;
    @NotBlank
    private String senderEmail;
    @NotBlank
    private String replyTo;
    @NotBlank
    private String subject;
    @NotNull
    private String timestamp;
    @NotNull
    private List<String> body;
    @NotNull
    private List<Link> links;
    @NotNull
    private List<Image> images;
    @NotBlank
    private String signature;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Link {
    @NotBlank
    private String id;
    @NotBlank
    private String text;
    @NotBlank
    private String url;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Image {
    @NotBlank
    private String id;
    @NotBlank
    private String url;
    @NotBlank
    private String alt;
  }
}