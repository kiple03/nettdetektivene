import { StrictMode, Suspense } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {router} from './routes/index.tsx'
import './index.css'
import MedalionWatcher from './components/miscellaneous/MedalionWatcher.tsx'
import BackgroundMusic from './components/ui/buttons/MusicMuteButton.tsx'
import AuthSessionRefresher from './components/auth/AuthSessionRefresher.tsx'

//Create client 
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // 5 min before refresh website
      staleTime: 5*60*1000,
    },
  },
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthSessionRefresher />
      <MedalionWatcher />
      <Suspense fallback={<div style={{ minHeight: '100vh', background: '#121f0f' }} />}>
        <RouterProvider router={router} />
      </Suspense>
      <BackgroundMusic />
    </QueryClientProvider>
  </StrictMode>,
)