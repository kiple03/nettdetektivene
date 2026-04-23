import DetectiveBook from "@/components/game/ui/DetectiveBook.tsx";
import DetectiveTipsNote from "@/components/game/ui/DetectiveTipsNote.tsx";
import PictureGrid from "@/components/game/tasks/PictureGrid";
import { submitMultiSelectAnswer } from "@/api/gameApi";
import { mapSubmitResponseToSubmitAnswer } from "@/utils/gameMapper";
import type { StationData, SubmitAnswerResult } from "@/types/station";

type PhotographerStationProps = {
    station: StationData;
    onAnswer: (result: SubmitAnswerResult) => void;
    disabled?: boolean;
};

const detectiveTips = [
    "Les teksten nøye",
    "Se etter kilder og detaljer",
    "Sammenlign fakta",
    "Hva virker troverdig?",
];

export default function PhotographerStation({
                                                station,
                                                onAnswer,
                                                disabled = false,
                                            }: PhotographerStationProps) {
    const handleSubmit = async (selectedIds: number[]) => {
        try {
            const response = await submitMultiSelectAnswer(station.questionId, {
                shape: "MULTIPLE_CHOICE",
                selectedIds,
            });

            onAnswer(mapSubmitResponseToSubmitAnswer(response));
        } catch (error: any) {
            console.error("Feil ved innsending", error);

            onAnswer({
                correct: false,
                feedback:
                    error?.response?.data?.message ??
                    "Noe gikk galt ved innsending",
            });
        }
    };

    return (
        <DetectiveBook
            title="Ekte, KI eller manipulert?"
            subtitle="Ser dette bildet AI generert ut?"
            tipsSlot={
                <DetectiveTipsNote
                    title="Detektivtips"
                    tips={detectiveTips}
                />
            }
        >
            <PictureGrid
                key={station.questionId}
                questionId={station.questionId}
                options={station.options.map((option) => ({
                    id: option.id,
                    imageUrl: option.imageUrl ?? "",
                    altText: option.label ?? `Bilde ${option.id}`,
                }))}
                onSubmit={handleSubmit}
                disabled={disabled}
            />
        </DetectiveBook>
    );
}