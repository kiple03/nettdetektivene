import React, { useState } from "react";
import type { PostkontoretTask } from "@/types/email.ts";
import "@/components/game/ui_game/game.css";
import styles from "@/components/game/tasks/PictureGrid.module.css";
import type { SubmitResult } from "@/types/station.ts";

interface Props {
  task: PostkontoretTask;
  /** Called with the IDs of all parts the student flagged as suspicious. */
  onSubmit: (selectedIds: string[]) => void;
  disabled?: boolean;
  /** Highlight feedback after submission; correct/wrong parts are coloured accordingly. */
  result?: SubmitResult;
}

/**
 * Renders a fake Gmail-style email where students click suspicious parts
 * (subject, sender, body, links, images, signature) before submitting.
 */
export const EmailTask: React.FC<Props> = ({ task, onSubmit, disabled = false, result }) => {
  const { email } = task;
  const [selected, setSelected] = useState<string[]>([]);

  const handleSubmit = () => {
    onSubmit(selected);
    setSelected([]);
  };

  const toggleSelect = (id: string) => {
    if (disabled) return;
    setSelected((prev) =>
        prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    );
  };

  const getHighlightClass = (id: string): string => {
    if (!result) return selected.includes(id) ? "selected" : "";
    if (result.correctOptionIds.includes(id)) return "correct-highlight";
    if (result.wrongSelectedOptionIds.includes(id)) return "wrong-highlight";
    return "";
  };

  const formattedDate = new Date(email.timestamp).toLocaleString("nb-NO", {
    day: "2-digit", month: "short", year: "numeric",
    hour: "2-digit", minute: "2-digit"
  });

  return (
      <div className="monitor-frame">
        <div className="gmail-wrapper">
          <div className="gmail-header-bar">
            <div className="gmail-header-title">{email.subject}</div>
            <div className="gmail-header-actions">
              <span className="material-icon">⟳</span>
              <span className="material-icon">⋮</span>
            </div>
          </div>

          <div className="gmail-message">
            <div className="gmail-message-header">
              <div className={`gmail-subject clickable ${
                  getHighlightClass(("subject"))
              }`}
                   onClick={() => toggleSelect("subject")}
              >
                {email.subject}
              </div>
              <div className="gmail-timestamp">{formattedDate}</div>
            </div>

            <div className="gmail-from-row">
              <div className="gmail-avatar">S</div>
              <div className="gmail-from-info">
                <div className="gmail-from-line">
                  <span className="gmail-from-name">{email.senderName}</span>
                  <span
                      className={`gmail-from-email clickable ${
                        getHighlightClass("senderEmail")
                      }`}
                      onClick={() => toggleSelect("senderEmail")}
                  >
                    &lt;{email.senderEmail}&gt;
                  </span>
                </div>
                <div className="gmail-to-line">til meg</div>
              </div>
            </div>

            <div
                className={`gmail-body clickable ${
                    getHighlightClass("body")
                }`}
                onClick={() => toggleSelect("body")}
            >
              {email.body.map((p, idx) => (
                  <p key={idx}>{p}</p>
              ))}
            </div>

            {email.images && email.images.length > 0 && (
                <div className="gmail-images">
                  {email.images.map((img) => (
                      <img
                          key={img.id}
                          src={img.url}
                          alt={img.alt || ""}
                          className={`gmail-image clickable ${
                              getHighlightClass(img.id)
                          }`}
                          onClick={() => toggleSelect(img.id)}
                      />
                  ))}
                </div>
            )}

            {email.links && email.links.length > 0 && (
                <div className="gmail-links">
                  {email.links.map((link) => (
                      <button
                          key={link.id}
                          type="button"
                          className={`gmail-link-button clickable ${
                              getHighlightClass(link.id)
                          }`}
                          onClick={() => toggleSelect(link.id)}
                      >
                        {link.text}
                        <span className="show-button-link">{link.url}</span>
                      </button>
                  ))}
                </div>
            )}

            {email.signature && (
                <div
                    className={`gmail-signature clickable ${
                        getHighlightClass("signature")
                    }`}
                    onClick={() => toggleSelect("signature")}
                >
                  {email.signature.split("\n").map((line, idx) => (
                      <div key={idx}>{line}</div>
                  ))}
                </div>
            )}
          </div>
        </div>
        <div className="gmail-result">
          <button
              className={styles.submitButton}
              onClick={handleSubmit}
              disabled={disabled || selected.length === 0}
          >
            Send inn svar
          </button>
        </div>
      </div>
  );
};