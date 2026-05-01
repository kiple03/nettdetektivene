package org.ntnu.backend.game.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a post-office (phishing email analysis) question with answer options and correct suspicious elements.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostOfficeQuestion {

  /** The email to analyse for suspicious elements. */
  @NotNull
  private Email email;
  /** Answer options the student can select as suspicious. */
  @NotNull
  private List<PostOfficeOption> options;
  /** IDs of options that are actually suspicious (correct answers). */
  @NotNull
  private List<String> correctSuspicious;
  /** Advice shown after the student submits their answer. */
  @NotBlank
  private String recommendedAction;

  /** Represents the simulated email shown in the question. */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Email {
    /** Display name of the sender. */
    @NotBlank
    private String senderName;
    /** From-address of the sender. */
    @NotBlank
    private String senderEmail;
    /** Reply-to address. */
    @NotBlank
    private String replyTo;
    @NotBlank
    private String subject;
    @NotNull
    private String timestamp;
    /** Paragraphs of the email body. */
    @NotNull
    private List<String> body;
    @NotNull
    private List<Link> links;
    @NotNull
    private List<Image> images;
    @NotBlank
    private String signature;
  }

  /** A hyperlink embedded in the email. */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Link {
    /** Unique identifier used to reference this link as a suspicious element. */
    @NotBlank
    private String id;
    /** Visible link text. */
    @NotBlank
    private String text;
    @NotBlank
    private String url;
  }

  /** An image embedded in the email. */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Image {
    /** Unique identifier used to reference this image as a suspicious element. */
    @NotBlank
    private String id;
    @NotBlank
    private String url;
    /** Alt text / accessible description. */
    @NotBlank
    private String alt;
  }
}