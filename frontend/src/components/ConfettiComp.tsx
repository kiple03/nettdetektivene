import Confetti from 'react-confetti';
import { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';

function ConfettiComp({duration = 5000}){
    const [isActive, setIsActive] = useState(true);
    useEffect(() => {
        const timer = setTimeout(() => {setIsActive(false);}, duration);
        return () => clearTimeout(timer);
    }, [duration]);

    return createPortal(
        <Confetti
            recycle={isActive}
            width={window.innerWidth}
            height={window.innerHeight}
            style={{ position: 'fixed', top: 0, left: 0, pointerEvents: 'none', zIndex: 9999 }}
        />,
        document.body
    );
}

export default ConfettiComp;