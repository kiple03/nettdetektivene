import styles from "./NotesButton.module.css";
import noteImg from "@/assets/images/studentHomeBoard/notebook.png";

type Props = {
    onClick: () => void;
};

/** Button that opens the student's notebook overlay. */
export default function NotesButton({ onClick }: Props) {
    return (
        <button
            className={styles.btn}
            onClick={onClick}
            aria-label="Åpne notatblokken"
            title="Notatblokk"
        >
            <img src={noteImg} alt="" className={styles.btnImg} />
        </button>
    );
}
