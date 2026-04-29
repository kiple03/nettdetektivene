import { useEffect, useState } from "react";
import styles from './MedalionToast.module.css';

type MedalionToastProp = {
    name: string,
    description: string,
    imageUrl: string,
    duration?: number,
    onDone?: () => void;
};

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