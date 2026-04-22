import { useEffect, useState } from "react";
import styles from './MedalionToast.module.css';

type MedalionToastProp = {
    name: string,
    description: string,
    imageUrl: string,
    duration?: number,
    onDone?: () => void;
};

function MedalionToast({name, description, imageUrl, duration=4000, onDone}: MedalionToastProp){
    const [visible, setVisible] = useState(true);

    useEffect(() => {
        const timer = setTimeout(() => {
            setVisible(false);
            onDone?.();

        }, duration);
        return () => clearTimeout(timer);
    }, [duration, onDone]);

    if(!visible) return null;

    return (      
      <div className={styles['medalion-toast']}>
        <img src={imageUrl} alt={name} className="medalion-toast__image" />
        <div className={styles['medalion-toast__text']}>                             
      <p className={styles['medalion-toast__title']}>🏅 Achievement unlocked!</p>
      <p className={styles['medalion-toast__name']}>{name}</p>                                                                                                                       
      <p className={styles['medalion-toast__description']}>{description}</p>
    </div>                                                       
      </div>                                                                                                                                                                         
    ); 
}
export default MedalionToast;