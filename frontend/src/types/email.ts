/** An image embedded in an email. */
export interface EmailImage {
  id: string;
  url: string;
  alt?: string;
}

/** A hyperlink within an email body. */
export interface EmailLink {
  id: string;
  text: string;
  url: string;
}

/** Full content of a simulated email used in game tasks. */
export interface EmailContent {
  senderName: string;
  senderEmail: string;
  replyTo: string;
  subject: string;
  /** ISO 8601 timestamp string. */
  timestamp: string;
  /** Paragraphs of the email body. */
  body: string[];
  images?: EmailImage[];
  links?: EmailLink[];
  signature?: string;
}

/** A Postkontoret station task backed by a simulated phishing/suspicious email. */
export interface PostkontoretTask {
  email: EmailContent;
  /** IDs or labels of elements the student should flag as suspicious. */
  correctSuspicious: string[];
  recommendedAction: string;
}
