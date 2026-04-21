import mapImg from '@/assets/images/studentHomeBoard/map.png';
import badgeImg from '@/assets/images/studentHomeBoard/badge.png';
import noteImg from '@/assets/images/studentHomeBoard/notebook.png';
import mysteryImg from '@/assets/images/studentHomeBoard/mystery.png';
import profileImg from '@/assets/images/studentHomeBoard/profile.png';

import type { DashboardItem } from "@/types/dashboard.ts";

export const studentBoardItems: DashboardItem[] = [
  {
    id: 'map',
    label: 'Kart',
    to: '/map',
    top: '50%',
    left: '50%',
    image: mapImg,
    size: 'large'
  },
  {
    id: 'badges',
    label: 'Medaljer',
    to: '/badges',
    top: '70%',
    left: '25%',
    image: badgeImg,
    size: 'small'
  },
  {
    id: 'leaderBoard',
    label: 'Ledertavle',
    to: '/leaderboard',
    top: '90%',
    left: '40%'
  },
  {
    id: 'notebook',
    label: 'Viktige notater!',
    to: '/notes',
    top: '75%',
    left: '70%',
    image: noteImg,
    size: 'medium'
  },
  {
    id: 'mystery',
    label: 'Ukens mysterium',
    to: '/weekly-mystery',
    top: '35%',
    left: '70%',
    image: mysteryImg,
    size: 'small'
  },
  {
    id: 'profile',
    label: 'Profil',
    to: '/profile',
    top: '35%',
    left: '28%',
    image: profileImg,
    size: 'medium'
  }
];