
export type DashboardItem = {
  id: string,
  label: string,
  to: string,
  top: string,
  left: string,
  image?: string;
  size?: 'small' | 'medium' | 'large',
  circle?: string
}