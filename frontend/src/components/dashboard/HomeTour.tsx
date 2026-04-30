import { useState, useCallback, type KeyboardEvent } from 'react';
import type { TourStep } from '@/data/studentTourSteps';
import styles from './HomeTour.module.css';

interface HomeTourProps {
  steps: TourStep[];
  onExit: () => void;
}

const TOOLTIP_WIDTH = 300;
const TOOLTIP_HEIGHT_ESTIMATE = 210;
const MARGIN = 12;

function HomeTour({ steps, onExit }: HomeTourProps) {
  const [index, setIndex] = useState(0);
  const [rect, setRect] = useState<DOMRect | null>(null);

  const measureTarget = (order: number) => {
    const el = document.querySelector(`[data-tour-order="${order}"]`);
    if (el) setRect(el.getBoundingClientRect());
    else setRect(null);
  };

  // Callback ref: focus overlay and measure first target without useEffect
  const overlayRef = useCallback((node: HTMLDivElement | null) => {
    if (!node) return;
    node.focus();
    const el = document.querySelector(`[data-tour-order="${steps[0].tourOrder}"]`);
    if (el) setRect(el.getBoundingClientRect());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handlePrev = () => {
    const newIndex = index - 1;
    measureTarget(steps[newIndex].tourOrder);
    setIndex(newIndex);
  };

  const handleNext = () => {
    if (index === steps.length - 1) {
      onExit();
      return;
    }
    const newIndex = index + 1;
    measureTarget(steps[newIndex].tourOrder);
    setIndex(newIndex);
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
    if (e.key === 'Escape') { onExit(); return; }
    if (e.key === 'ArrowLeft' && index > 0) handlePrev();
    if (e.key === 'ArrowRight') handleNext();
  };

  const step = steps[index];
  const isFirst = index === 0;
  const isLast = index === steps.length - 1;

  const tooltipStyle = (() => {
    if (!rect) return {};
    const flipAbove = rect.bottom + TOOLTIP_HEIGHT_ESTIMATE > window.innerHeight - MARGIN;
    const top = flipAbove
      ? rect.top - TOOLTIP_HEIGHT_ESTIMATE - MARGIN
      : rect.bottom + MARGIN;
    const rawLeft = rect.left + rect.width / 2 - TOOLTIP_WIDTH / 2;
    const left = Math.max(MARGIN, Math.min(rawLeft, window.innerWidth - TOOLTIP_WIDTH - MARGIN));
    return { top, left, width: TOOLTIP_WIDTH };
  })();

  return (
    <div
      ref={overlayRef}
      className={styles.overlay}
      onKeyDown={handleKeyDown}
      tabIndex={-1}
      role="dialog"
      aria-modal="true"
      aria-label="Guidet omvisning"
    >
      {rect && (
        <div
          className={styles.highlight}
          style={{
            top: rect.top,
            left: rect.left,
            width: rect.width,
            height: rect.height,
          }}
        />
      )}

      {rect && (
        <div className={styles.tooltip} style={tooltipStyle}>
          <button
            className={styles.btnClose}
            onClick={onExit}
            aria-label="Avslutt omvisning"
          >
            ×
          </button>
          <span className={styles.stepCounter}>{index + 1} / {steps.length}</span>
          <h2 className={styles.title}>{step.title}</h2>
          <p className={styles.body}>{step.body}</p>
          <div className={styles.buttons}>
            <button
              className={styles.btnSecondary}
              onClick={handlePrev}
              disabled={isFirst}
            >
              Forrige
            </button>
            <button
              className={styles.btnPrimary}
              onClick={handleNext}
            >
              {isLast ? 'Ferdig' : 'Neste'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default HomeTour;
