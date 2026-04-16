import {type StationData, StationType} from "@/types/station.ts";
import fakeNewsImage from "@/assets/images/fake_news.png"
import realNewsImage from "@/assets/images/vg_news.png"

export const newsMock: StationData = {
  id: 1,
  type: StationType.NEWS,
  title: "Nyhetskvartal - den falske forsiden",
  questionText: "To avisforsider ligger foran deg. En er ekte, en er laget for å lure deg." +
      " Kan du finne ut hvilken som er falsk?",
  options: [
    {
      id: "1",
      imageUrl: fakeNewsImage,
      isCorrect: true,
      feedbackTitle: "God jobbet, detektiv!",
      feedbackText: "Dette bildet er manipulert og bruker store skumle ord for å skape frykt",
    },
    {
      id: "2",
      imageUrl: realNewsImage,
      isCorrect: false,
      feedbackTitle: "Ikke helt, her er forklaringen!",
      feedbackText: "Den andre avisartikkelen bruker klassisk konspirasjonsretorikk....",
    },
  ]
}