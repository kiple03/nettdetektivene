import mapImg from '@/assets/images/studentHomeBoard/map.png';
import badgeImg from '@/assets/images/studentHomeBoard/badge.png';
import noteImg from '@/assets/images/studentHomeBoard/notebook.png';
import mysteryImg from '@/assets/images/studentHomeBoard/mystery.png';
import profileImg from '@/assets/images/studentHomeBoard/profile.png';
import leaderBoard from '@/assets/images/studentHomeBoard/leaderBoard.png'
import circle from '@/assets/images/studentHomeBoard/red_circle.png'

import type { DashboardItem } from "@/types/dashboard.ts";

export const studentBoardItems: DashboardItem[] = [
  {
    id: 'map',
    label: 'Kart',
    to: '/map',
    top: '35%',
    left: '51%',
    image: mapImg,
    size: 'large',
    circle: circle
  },
  {
    id: 'badges',
    label: 'Medaljer',
    to: '/badges',
    top: '75%',
    left: '49%',
    image: badgeImg,
    size: 'medium'
  },
  {
    id: 'leaderBoard',
    label: 'Ledertavle',
    to: '/leaderboard',
    top: '75%',
    image: leaderBoard,
    left: '25%',
    size: 'small'
  },
  {
    id: 'notebook',
    label: 'Viktige notater!',
    to: '/notes',
    top: '75%',
    left: '75%',
    image: noteImg,
    size: 'medium'
  },
  {
    id: 'mystery',
    label: 'Ukens mysterium',
    to: '/weekly-mystery',
    top: '34%',
    left: '85%',
    image: mysteryImg,
    size: 'medium'
  },
  {
    id: 'profile',
    label: 'Profil',
    to: '/profile',
    top: '34%',
    left: '15%',
    image: profileImg,
    size: 'medium'
  }
];