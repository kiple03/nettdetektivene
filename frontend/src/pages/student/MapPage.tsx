import { type CSSProperties, useState, useMemo } from "react";
import { useNavigate, useSearchParams, useLocation } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { useQuery } from "@tanstack/react-query";
import { useUserProfile } from "@/hooks/useUserProfile";
import { getGameStops } from "@/api/gameApi";
import { studentKeys } from "@/hooks/student/queryKeys";
import type { EquippedItem } from "@/types/user";
import styles from "./MapPage.module.css";
import { StopMap } from "@/components/map/StopMap";
import { MAP_STOPS, type StopData } from "@/data/mapStops";
import internettbyenLogo from "@/assets/images/map/internettbyen-logo.png";
import detektivkontoret from "@/assets/images/map/detektivkontoret.png";
import { CarWithAvatar } from "@/components/map/CarWithAvatar";
import sharkBase from "@/assets/images/boss/shark-base.png";
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog/Alert-Dialog.tsx";
import { Button } from "@/components/ui/buttons/Button.tsx";
import LockBadge from "@/components/ui/badges/Lock-Badge.tsx";
import { StopLevelsModal } from "@/components/map/StopLevelsModal";
import { MapLoadingOverlay } from "@/components/map/MapLoadingOverlay";

export default function MapPage() {
  const { email } = useAuth();
  const { data: profile } = useUserProfile();
  const { data: backendStops = [] } = useQuery({
    queryKey: studentKeys.gameStops,
    queryFn: getGameStops,
  });

  const isUnlocked = (title: string) => {
    if (!backendStops.length) return false;
    const backend = backendStops.find(
      (s) => s?.name?.toLowerCase() === title.toLowerCase()
    );
    return backend ? !backend.locked : false;
  };

  const stopIdByTitle = Object.fromEntries(
    backendStops.map((s) => [s.name, s.id])
  );
  const equippedItems: EquippedItem[] = profile?.equippedItems ?? [];
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const autoStopId = Number(searchParams.get("levelId")) || null;
  const [lockedDialogOpen, setLockedDialogOpen] = useState(false);
  const [lockedStopTitle, setLockedStopTitle] = useState("");
  const [adminBypassInput, setAdminBypassInput] = useState("");
  const carOffsetKey = `carPathOffset_${email ?? "anonymous"}`;
  const [currentPathOffset, setCurrentPathOffset] = useState(
    () => localStorage.getItem(`carPathOffset_${email ?? "anonymous"}`) ?? "5%"
  );
  const [animating, setAnimating] = useState(false);
  const [carReversed, setCarReversed] = useState(false);
  const [carOffsetPath, setCarOffsetPath] = useState<string | null>(null);
  const [manualPendingStop, setManualPendingStop] = useState<StopData | null>(
    null
  );

  const autoStop = useMemo(() => {
    if (!autoStopId || !backendStops.length) return null;
    const match = backendStops.find((s) => s.id === autoStopId);
    if (!match) return null;
    return MAP_STOPS.find((l) => l.title === match.name) ?? null;
  }, [autoStopId, backendStops]);

  const pendingStop = manualPendingStop ?? autoStop;

  const animateTo = (offset: string, onDone: () => void) => {
    if (animating) return;
    if (offset === currentPathOffset) {
      onDone();
      return;
    }
    setCarReversed(parseFloat(offset) < parseFloat(currentPathOffset));
    setAnimating(true);
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        setCurrentPathOffset(offset);
        localStorage.setItem(carOffsetKey, offset);
        setTimeout(() => {
          setAnimating(false);
          setCarReversed(false);
          onDone();
        }, 1200);
      });
    });
  };

  const handleAdminBypassInput = (value: string) => {
    setAdminBypassInput(value);
    if (value === "admin") {
      setLockedDialogOpen(false);
      setAdminBypassInput("");
      navigate("/boss");
    }
  };

  const handleStopClick = (stop: StopData) => {
    animateTo(stop.pathOffset, () => {
      if (stop.title === "Datasenteret" && isUnlocked(stop.title)) {
        navigate("/boss");
        return;
      }
      if (isUnlocked(stop.title)) {
        setManualPendingStop(stop);
      } else {
        setLockedStopTitle(stop.title);
        setLockedDialogOpen(true);
      }
    });
  };

  const handleBackClick = () => animateTo("0%", () => navigate("/home"));

  return (
    <>
      {location.state?.fromHome && <MapLoadingOverlay />}
      <StopMap
        className={styles.mapContainer}
        stops={MAP_STOPS}
        onStopClick={handleStopClick}
        onBackClick={handleBackClick}
        backIcon={
          <img
            src={detektivkontoret}
            alt="Tilbake til Detektivkontoret"
            className={styles.icon}
          />
        }
        onCarPathReady={setCarOffsetPath}
        header={
          <div className={styles.header}>
            <img
              src={internettbyenLogo}
              alt="Internettbyen"
              className={styles.headerLogo}
            />
          </div>
        }
        renderStopIcon={(stop) => {
          if (stop.title === "Datasenteret") {
            return (
              <>
                <div className={styles.datasenteretIcons}>
                  <img
                    src={stop.icon}
                    alt={stop.title}
                    className={`${styles.icon} ${
                      !isUnlocked(stop.title) ? styles.lockedIcon : ""
                    }`}
                  />

                  <div className={styles.sharkWrapper}>
                    <img
                      src={sharkBase}
                      alt="Shark"
                      className={`${styles.icon} ${
                        !isUnlocked(stop.title) ? styles.sharkSilhouette : ""
                      }`}
                    />

                    {/* only show ? when locked */}
                    {!isUnlocked(stop.title) && (
                      <span className={styles.sharkQuestionMark}>?</span>
                    )}
                  </div>
                </div>

                {!isUnlocked(stop.title) && <LockBadge />}
              </>
            );
          }
          return (
            <>
              <img
                src={stop.icon}
                alt={stop.title}
                className={`${styles.icon} ${
                  !isUnlocked(stop.title) ? styles.lockedIcon : ""
                }`}
              />
              {!isUnlocked(stop.title) && <LockBadge />}
            </>
          );
        }}
      >
        <div
          className={`${styles.car} ${animating ? styles.carAnimating : ""}`}
          style={
            {
              "--car-offset": currentPathOffset,
              ...(carOffsetPath
                ? { offsetPath: `path("${carOffsetPath}")` }
                : {}),
            } as CSSProperties
          }
        >
          <div className={carReversed ? styles.carFlipped : undefined}>
            <CarWithAvatar
              items={equippedItems}
              size={140}
              hairColor={profile?.hairColor}
            />
          </div>
        </div>
      </StopMap>

      <StopLevelsModal
        stop={
          pendingStop
            ? {
                title: pendingStop.title,
                stopId: stopIdByTitle[pendingStop.title] ?? null,
              }
            : null
        }
        onClose={() => {
          setManualPendingStop(null);
          if (searchParams.has("levelId")) setSearchParams({});
        }}
      />

      <AlertDialog
        open={lockedDialogOpen}
        onOpenChange={(open) => {
          setLockedDialogOpen(open);
          if (!open) setAdminBypassInput("");
        }}
      >
        <AlertDialogContent>
          <AlertDialogTitle>Nivå låst 🔒</AlertDialogTitle>
          <AlertDialogDescription>
            {lockedStopTitle} er ikke åpnet ennå. Du må fullføre de tidligere
            nivåene først.
          </AlertDialogDescription>
          {lockedStopTitle === "Datasenteret" && (
            <input
              type="text"
              className={styles.adminBypassInput}
              value={adminBypassInput}
              onChange={(e) => handleAdminBypassInput(e.target.value)}
              autoComplete="off"
              aria-label="admin"
            />
          )}
          <div className={styles.dialogActions}>
            <Button
              variant="fun"
              type="button"
              onClick={() => setLockedDialogOpen(false)}
            >
              Lukk
            </Button>
          </div>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
