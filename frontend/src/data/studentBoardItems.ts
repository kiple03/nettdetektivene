import mapImg from '@/assets/images/studentHomeBoard/map.png';
import badgeImg from '@/assets/images/studentHomeBoard/badge.png';
import noteImg from '@/assets/images/studentHomeBoard/notebook.png';
import mysteryImg from '@/assets/images/studentHomeBoard/mystery.png';
import profileImg from '@/assets/images/studentHomeBoard/profile.png';
import leaderBoard from '@/assets/images/studentHomeBoard/leaderBoard.png'
import circle from '@/assets/images/studentHomeBoard/red_circle.png'
import kommerSnart from '@/assets/images/studentHomeBoard/kommer_snart.png'

import type { DashboardItem } from "@/types/dashboard.ts";

/** Navigation items shown on the student home dashboard board. */
export const studentBoardItems: DashboardItem[] = [
  {
    id: 'profile',
    label: 'Profil',
    to: '/profile',
    top: '34%',
    left: '15%',
    image: profileImg,
    size: 'medium',
    tourOrder: 1,
  },
  {
    id: 'map',
    label: 'Kart',
    to: '/map',
    top: '35%',
    left: '51%',
    image: mapImg,
    size: 'large',
    circle: circle,
    tourOrder: 3,
  },
  {
    id: 'mystery',
    label: 'Ukens mysterium',
    to: '/weekly-mystery',
    top: '34%',
    left: '85%',
    image: mysteryImg,
    size: 'medium',
    overlay: kommerSnart
  },
  {
    id: 'leaderBoard',
    label: 'Ledertavle',
    to: '/leaderboard',
    top: '75%',
    image: leaderBoard,
    left: '25%',
    size: 'small',
    tourOrder: 2,
  },
  {
    id: 'badges',
    label: 'Medaljer',
    to: '/badges',
    top: '75%',
    left: '49%',
    image: badgeImg,
    size: 'medium',
    tourOrder: 4,
  },
  {
    id: 'notebook',
    label: 'Notatbok',
    to: '/notes',
    top: '75%',
    left: '75%',
    image: noteImg,
    size: 'medium',
    tourOrder: 5,
  }
];