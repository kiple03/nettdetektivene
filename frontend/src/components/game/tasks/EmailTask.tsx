import React, { useState } from "react";
import type { PostkontoretTask } from "@/types/email.ts";
import "@/components/game/ui_game/game.css";
import styles from "@/components/game/tasks/PictureGrid.module.css";
import type { SubmitResult } from "@/types/station.ts";

interface Props {
  task: PostkontoretTask;
  onSubmit: (selectedIds: string[]) => void;
  disabled?: boolean;
  result?: SubmitResult;
}

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
            <button
              type="button"
              className={`gmail-subject btn-unstyled clickable ${getHighlightClass("subject")}`}
              onClick={() => toggleSelect("subject")}
              aria-pressed={selected.includes("subject")}
            >
              {email.subject}
            </button>
              <div className="gmail-timestamp">{formattedDate}</div>
            </div>

            <div className="gmail-from-row">
              <div className="gmail-avatar">S</div>
              <div className="gmail-from-info">
                <div className="gmail-from-line">
                  <span className="gmail-from-name">{email.senderName}</span>
                  <button
                    type="button"
                    className={`gmail-from-email btn-unstyled-inline clickable ${getHighlightClass("senderEmail")}`}
                    onClick={() => toggleSelect("senderEmail")}
                    aria-pressed={selected.includes("senderEmail")}
                  >
                    &lt;{email.senderEmail}&gt;
                  </button>
                </div>
                <div className="gmail-to-line">til meg</div>
              </div>
            </div>

            <button
              type="button"
              className={`gmail-body btn-unstyled clickable ${getHighlightClass("body")}`}
              onClick={() => toggleSelect("body")}
              aria-pressed={selected.includes("body")}
            >
              {email.body.map((p, idx) => <p key={idx}>{p}</p>)}
            </button>

            {email.images && email.images.length > 0 && (
                <div className="gmail-images">
                  {email.images.map((img) => (
                      <button
                        key={img.id}
                        type="button"
                        className={`gmail-image-btn btn-unstyled clickable ${getHighlightClass(img.id)}`}
                        onClick={() => toggleSelect(img.id)}
                        aria-pressed={selected.includes(img.id)}
                      >
                        <img src={img.url} alt={img.alt || ""} className="gmail-image" />
                      </button>
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
                <button
                  type="button"
                  className={`gmail-signature btn-unstyled clickable ${getHighlightClass("signature")}`}
                  onClick={() => toggleSelect("signature")}
                  aria-pressed={selected.includes("signature")}
                >
                  {email.signature.split("\n").map((line, idx) => <div key={idx}>{line}</div>)}
                </button>
            )}
          </div>
        </div>
        <div className="gmail-result">
          <button
            type="button"
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