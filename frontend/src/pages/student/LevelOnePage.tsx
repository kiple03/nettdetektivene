import { useState, useEffect} from 'react';
import apiClient from '@/api/client';
import styles from './LevelOnePage.module.css';

type Option = {
    id: number;
    text: string;
};

type Question = {
    id: number;
    prompt: string;
    options: Option[];
};

export default function Level1() {
    const [question, setQuestion] = useState<Question | null>(null);
    const [selectedOptionId, setSelectedOptionId] = useState<number | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [feedback, setFeedback] = useState<{ isCorrect: boolean; message: string} | null>(null);

    //MUST IMPLEMENT fetch data from backend
    useEffect(() => {
        setQuestion({
            id: 1,
            prompt: "Am I cool?",
            options: [
                {id: 1, text: "Yæh"},
                {id: 2, text: "næh"},
            ]
        });
    }, []);

    // MUST IMPLEMENT submitting answer to backend
    const handleSubmit = async () => {
        if(selectedOptionId === null || !question) return;

        setIsSubmitting(true);
        setFeedback(null);

        try{
            const response = await apiClient.post(`/levels/submit-answer`, {
                questionId: question.id,
                optionId: selectedOptionId
            });

            setFeedback({
                isCorrect: response.data.isCorrect,
                message: response.data.message || (response.data.isCorrect ? "Korrekt!" : "Feil svar, prøv igjen.")
            });
        } catch(error){
            console.error("Failed to submit answer", error);
            setFeedback({isCorrect: false, message: "Error upon submitting answer."});
        } finally{
            setIsSubmitting(false);
        }
    };

    if(!question){
        return <div>Loading question...</div>;
    }
    
    return (
        <div className={styles.container}>
            <h1 className={styles.title}>Level 1</h1>
            <h2 className={styles.prompt}>{question.prompt}</h2>
            
            <div className={styles.optionsContainer}>
                {question.options.map((option) => (
                    <button key={option.id} onClick={() => setSelectedOptionId(option.id)} className={`${styles.optionButton} ${selectedOptionId === option.id ? styles.selected : ''}`}>
                        {option.text}
                    </button>
                ))}
            </div>

            <button onClick={handleSubmit} disabled={selectedOptionId === null || isSubmitting} className={styles.submitButton}>
                {isSubmitting ? 'Sjekker...' : 'Lever svar'}
            </button>

            {feedback && (
                <div className={`${styles.container} ${feedback.isCorrect ? styles.correct : styles.incorrect}`}>
                    <b>{feedback.isCorrect ? 'Bra!' : 'Feil'}</b> {feedback.message}
                </div>
            )}
        </div>
    );
}
