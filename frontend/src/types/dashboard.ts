
/** A single clickable item rendered on the student or teacher dashboard board. */
export type DashboardItem = {
  id: string,
  label: string,
  /** React Router destination path. */
  to: string,
  /** CSS top position (e.g. "20%") for absolute placement on the board. */
  top: string,
  /** CSS left position (e.g. "30%") for absolute placement on the board. */
  left: string,
  image?: string;
  size?: 'small' | 'medium' | 'large',
  /** Optional CSS class or value used to render the item as a circle shape. */
  circle?: string,
  overlay?: string,
  onClick?: () => void,
  /** Position in the guided tour sequence; omit to exclude from tour. */
  tourOrder?: number,
}