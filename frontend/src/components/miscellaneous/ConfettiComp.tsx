import Confetti from 'react-confetti';
import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';

type ConfettiCompProps = {
    duration?: number;
};

function ConfettiComp({ duration = 5000 }: ConfettiCompProps) {
    const [isActive, setIsActive] = useState(true);
    const [prefersReducedMotion, setPrefersReducedMotion] = useState(false);
    const [windowSize, setWindowSize] = useState({
        width: window.innerWidth,
        height: window.innerHeight,
    });

    useEffect(() => {
        const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');

        const updateReducedMotionPreference = () => {
            setPrefersReducedMotion(mediaQuery.matches);
        };

        updateReducedMotionPreference();

        mediaQuery.addEventListener('change', updateReducedMotionPreference);

        return () => {
            mediaQuery.removeEventListener('change', updateReducedMotionPreference);
        };
    }, []);

    useEffect(() => {
        const updateWindowSize = () => {
            setWindowSize({
                width: window.innerWidth,
                height: window.innerHeight,
            });
        };

        window.addEventListener('resize', updateWindowSize);

        return () => {
            window.removeEventListener('resize', updateWindowSize);
        };
    }, []);

    useEffect(() => {
        if (prefersReducedMotion) {
            return;
        }

        const timer = setTimeout(() => {
            setIsActive(false);
        }, duration);

        return () => clearTimeout(timer);
    }, [duration, prefersReducedMotion]);

    if (prefersReducedMotion) {
        return null;
    }

    return createPortal(
        <Confetti
            recycle={isActive}
            width={windowSize.width}
            height={windowSize.height}
            style={{
                position: 'fixed',
                top: 0,
                left: 0,
                pointerEvents: 'none',
                zIndex: 9999,
            }}
        />,
        document.body,
    );
}

export default ConfettiComp;
