import mapImg from '@/assets/images/studentHomeBoard/map.png';

export const teacherBoardItems = [
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
    label: 'Mine klasserom',
    to: '/mineklasserom',
    top: '80%',
    left: '65%'
  },
];
