type Props = {
  title: String;
  children: React.ReactNode;
};

function StationContainer({ title, children }: Props) {
  return (
      <div className="station-container">
        <h1>{title}</h1>

        <div className="station-content">
          {children}
        </div>
      </div>
  );
}

export default StationContainer;