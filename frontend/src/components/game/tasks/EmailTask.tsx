import React, { useState } from "react";
import type { PostkontoretTask } from "@/types/email.ts";
import "@/components/game/ui/game.css";
import styles from "@/components/game/tasks/PictureGrid.module.css";

interface Props {
  task: PostkontoretTask;
  onSubmit: (selectedIds: string[]) => void;
  disabled?: boolean;
}

export const EmailTask: React.FC<Props> = ({ task, onSubmit, disabled = false }) => {
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

  const formattedDate = new Date(email.timestamp).toLocaleString("nb-NO", {
    day: "2-digit", month: "short", year: "numeric",
    hour: "2-digit", minute: "2-digit"
  });

  return (
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
            <div
                className={`gmail-subject clickable ${
                    selected.includes("subject") ? "selected" : ""
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
                        selected.includes("senderEmail") ? "selected" : ""
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
                  selected.includes("body") ? "selected" : ""
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
                            selected.includes(img.id) ? "selected" : ""
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
                            selected.includes(link.id) ? "selected" : ""
                        }`}
                        onClick={() => toggleSelect(link.id)}
                    >
                      {link.text}
                    </button>
                ))}
              </div>
          )}

          {email.signature && (
              <div
                  className={`gmail-signature clickable ${
                      selected.includes("signature") ? "selected" : ""
                  }`}
                  onClick={() => toggleSelect("signature")}
              >
                {email.signature.split("\n").map((line, idx) => (
                    <div key={idx}>{line}</div>
                ))}
              </div>
          )}
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