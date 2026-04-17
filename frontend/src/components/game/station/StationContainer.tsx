import type {StationType} from "@/types/station.ts";

type Props = {
  title: string;
  type: StationType;
  children: React.ReactNode;
};

function StationContainer({ title, type, children }: Props) {
  return (
      <div className={`station-container station-${type.toLowerCase()}`}>
        <h1>{title}</h1>

        <div className="station-content">
          {children}
        </div>
      </div>
  );
}

export default StationContainer;