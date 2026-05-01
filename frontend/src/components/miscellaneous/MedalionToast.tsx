import { useEffect, useState } from "react";
import styles from './MedalionToast.module.css';

/** Props for the MedalionToast component. */
type MedalionToastProp = {
    name: string,
    description: string,
    imageUrl: string,
    /** Milliseconds before the toast begins its exit animation. Defaults to 7000. */
    duration?: number,
    /** Called after the exit animation completes. */
    onDone?: () => void;
};

/**
 * Displays a temporary toast notification when a medalion (achievement) is unlocked.
 * Auto-dismisses after `duration` ms and calls `onDone` when the exit animation ends.
 */
function MedalionToast({name, description, imageUrl, duration=7000, onDone}: MedalionToastProp){
    const [leaving, setLeaving] = useState(false);

    useEffect(() => {
        const showTimer = setTimeout(() => setLeaving(true), duration);
        return () => clearTimeout(showTimer);
    }, [duration]);

    const handleAnimationEnd = () => {
        if (leaving) onDone?.();
    };



    return (
      <div
        className={`${styles['medalion-toast']} ${leaving ? styles['medalion-toast--leaving'] : ''}`}
        onAnimationEnd={handleAnimationEnd}
      >
        <img src={imageUrl} alt={name} className={styles['medalion-toast__image']} />
        <div className={styles['medalion-toast__text']}>                             
      <p className={styles['medalion-toast__title']}>🏅 Achievement unlocked!</p>
      <p className={styles['medalion-toast__name']}>{name}</p>                                                                                                                       
      <p className={styles['medalion-toast__description']}>{description}</p>
    </div>                                                       
      </div>                                                                                                                                                                         
    ); 
}
export default MedalionToast;