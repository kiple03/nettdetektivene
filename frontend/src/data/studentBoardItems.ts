import mapImg from '@/assets/map.png';
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
    top: '50%',
    left: '65%'
  },
  {
    id: 'leaderBoard',
    label: 'Ledertavle',
    to: '/leaderboard',
    top: '80%',
    left: '35%'
  },
  {
    id: 'notebook',
    label: 'Viktige notater!',
    to: '/notes',
    top: '80%',
    left: '65%'
  },
  {
    id: 'mystery',
    label: 'Ukens mysterium',
    to: '/weekly-mystery',
    top: '35%',
    left: '60%'
  }
];