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
    top: '39%',
    left: '51%',
    image: mapImg,
    size: 'large'
  },
  {
    id: 'badges',
    label: 'Medaljer',
    to: '/badges',
    top: '70%',
    left: '49%',
    image: badgeImg,
    size: 'small'
  },
  {
    id: 'leaderBoard',
    label: 'Ledertavle',
    to: '/leaderboard',
    top: '80%',
    left: '33%'
  },
  {
    id: 'notebook',
    label: 'Viktige notater!',
    to: '/notes',
    top: '68%',
    left: '66%',
    image: noteImg,
    size: 'medium'
  },
  {
    id: 'mystery',
    label: 'Ukens mysterium',
    to: '/weekly-mystery',
    top: '34%',
    left: '69%',
    image: mysteryImg,
    size: 'small'
  },
  {
    id: 'profile',
    label: 'Profil',
    to: '/profile',
    top: '34%',
    left: '31%',
    image: profileImg,
    size: 'medium'
  }
];