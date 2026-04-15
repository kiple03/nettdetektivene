
// TODO: Cecilie - Lage flere størrelser for bilder i hjemskjerm så du slipper å hardkode/duplisere!!!

export type DashboardItem = {
  id: string,
  label: string,
  to: string,
  top: string,
  left: string,
  image?: string;
  size?: 'small' | 'medium' | 'large';

}