export interface EmailImage {
  id: string;
  url: string;
  alt?: string;
}

export interface EmailLink {
  id: string;
  text: string;
  url: string;
}

export interface EmailContent {
  senderName: string;
  senderEmail: string;
  replyTo: string;
  subject: string;
  timestamp: string;
  body: string[];
  images?: EmailImage[];
  links?: EmailLink[];
  signature?: string;
}

export interface PostkontoretTask {
  email: EmailContent;
  correctSuspicious: string[];
  recommendedAction: string;
}
