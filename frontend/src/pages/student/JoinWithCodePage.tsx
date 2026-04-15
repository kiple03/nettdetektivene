import { useState } from "react";
import styles from "./JoinWithCodePage.module.css";

import detectiveImage from "@/assets/images/joinWithCode/detective.png";
import classroomBackground from "@/assets/images/joinWithCode/bgClassroomCode.png";

export default function JoinWithCodePage() {
    const [code, setCode] = useState("");

    const handleJoin = () => {
        console.log("Joining classroom with code:", code);
        //TODO: endpoints
    };

    return (
        <div
            className={styles.page}
            style={{ backgroundImage: `url(${classroomBackground})` }}
        >
            <div className={styles.overlay} />

            <div className={styles.wrapper}>
                <div className={styles.topBox}>
                    <p className={styles.topTitle}>
                        Er du klar for å spore opp datatyven?
                    </p>
                    <p className={styles.topText}>
                        Du trenger bare skrive inn koden til klasserommet for å bli med i jakten!!
                    </p>
                </div>

                <div className={styles.bottomSection}>
                    <div className={styles.detectiveBox}>
                        <img
                            src={detectiveImage}
                            alt="Detektiv"
                            className={styles.detectiveImage}
                        />
                    </div>

                    <div className={styles.formBox}>
                        <label htmlFor="classroom-code" className={styles.label}>
                            Skriv inn kode:
                        </label>

                        <input
                            id="classroom-code"
                            type="text"
                            value={code}
                            onChange={(e) => setCode(e.target.value)}
                            placeholder="F.eks. ABC123"
                            className={styles.input}
                        />

                        <button type="button" onClick={handleJoin} className={styles.button}>
                            Bli med
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}