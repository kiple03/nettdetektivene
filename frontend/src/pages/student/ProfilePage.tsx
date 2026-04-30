import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import BackButton from "@/components/ui/buttons/BackButton";
import mapStyles from "@/pages/student/MapPage.module.css";
import styles from "@/pages/student/ProfilePage.module.css";
import { useUserProfile } from "@/hooks/useUserProfile";
import { useAuth } from "@/hooks/useAuth";
import { useMyClassroom } from "@/hooks/student/useMyClassroom";
import { leaveClassroom } from "@/api/classroomApi";
import { AlertDialog } from "@/components/ui/alert-dialog/Alert-Dialog";
import { LayeredAvatar } from "@/components/miscellaneous/LayeredAvatar";
import detectiveRoom from "@/assets/detective_room_interior.png";
import { getGameStops, getLevelsForStop } from "@/api/gameApi";
import { studentKeys } from "@/hooks/student/queryKeys";
import nyhetskvartalet from "@/assets/stops/nyhetIcon.png";
import fotografen from "@/assets/stops/fotoIcon.png";
import postkontoret from "@/assets/stops/postIcon.png";
import passordbanken from "@/assets/stops/passIcon.png";
import markedsplassen from "@/assets/stops/markedIcon.png";
import moteplassen from "@/assets/stops/sosialIcon.png";
import datasenteret from "@/assets/stops/dataIcon.png";

const STOP_ICONS: Record<string, string> = {
  "Nyhetskvartalet": nyhetskvartalet,
  "Fotografen": fotografen,
  "Postkontoret": postkontoret,
  "Passordbanken": passordbanken,
  "Markedsplassen": markedsplassen,
  "Den sosiale møteplassen": moteplassen,
  "Datasenteret": datasenteret,
};

function ProfilePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [showLeaveConfirm, setShowLeaveConfirm] = useState(false);
  const { data: profile } = useUserProfile();
  const { email } = useAuth();
  const { data: classroom, isLoading: classroomLoading, isError: classroomError } = useMyClassroom();
  const { data: gameStops = [] } = useQuery({ queryKey: studentKeys.gameStops, queryFn: getGameStops });

  const leaveMutation = useMutation({
    mutationFn: leaveClassroom,
    onSuccess: () => {
      setShowLeaveConfirm(false);
      queryClient.invalidateQueries({ queryKey: studentKeys.myClassroom });
    },
  });

  const unlockedStops = gameStops.filter(s => !s.locked);
  const currentStop = unlockedStops.length > 0 ? unlockedStops[unlockedStops.length - 1] : null;

  const { data: stopLevels = [] } = useQuery({
    queryKey: studentKeys.stopLevels(currentStop?.id ?? null),
    queryFn: () => getLevelsForStop(currentStop!.id),
    enabled: !!currentStop,
  });

  const firstLockedLevel = stopLevels.find(l => l.locked);
  const currentLevelOrder = firstLockedLevel
    ? stopLevels.indexOf(firstLockedLevel)
    : stopLevels.length > 0
      ? stopLevels.length
      : null;

  return (
    <div className={styles.page}>
      <img src={detectiveRoom} alt="" className={styles.bg} />

      <BackButton
        className={mapStyles.backBtn}
        style={{ position: "fixed", top: "1rem", left: "1rem", zIndex: 50 }}
        ariaLabel="Gå tilbake"
      />

      <div className={styles.dossier}>
        <div className={styles.dossierHeader}>
          <span className={styles.stamp}>CASE FILE</span>
          <h1 className={styles.dossierTitle}>Detektiv</h1>
        </div>

        <div className={styles.avatarRing}>
          <LayeredAvatar
            items={profile?.equippedItems ?? []}
            size={180}
            hairColor={profile?.hairColor}
          />
        </div>

        <div className={styles.info}>
          <p className={styles.username}>{profile?.username ?? "…"}</p>
          {email && <p className={styles.email}>{email}</p>}
          <p className={styles.classroom}>
            {classroomLoading
              ? "…"
              : classroomError
                ? "Kunne ikke laste klasserom"
                : classroom
                  ? `Du er i klasserom: ${classroom.title}`
                  : "Ikke i et klasserom"}
          </p>
          {classroom && (
            <button
              className={styles.leaveBtn}
              onClick={() => setShowLeaveConfirm(true)}
            >
              Forlat klasserom
            </button>
          )}
        </div>

        <AlertDialog open={showLeaveConfirm} onOpenChange={setShowLeaveConfirm}>
          <div className={styles.confirmModal}>
            <p className={styles.confirmText}>
              Er du sikker på at du vil forlate klasserommet?
            </p>
            <div className={styles.confirmActions}>
              <button
                className={styles.cancelBtn}
                onClick={() => setShowLeaveConfirm(false)}
              >
                Avbryt
              </button>
              <button
                className={styles.confirmLeaveBtn}
                disabled={leaveMutation.isPending}
                onClick={() => leaveMutation.mutate()}
              >
                {leaveMutation.isPending ? "…" : "Forlat"}
              </button>
            </div>
          </div>
        </AlertDialog>

        {currentStop ? (
          <div className={styles.progressSection}>
            <div className={styles.progressStop}>
              <img
                src={STOP_ICONS[currentStop.name]}
                alt={currentStop.name}
                className={styles.stopIcon}
              />
              <div className={styles.progressDetails}>
                <span className={styles.progressLabel}>Nåværende stopp</span>
                <span className={styles.progressValue}>{currentStop.name}</span>
              </div>
            </div>
            {currentLevelOrder !== null && (
              <div className={styles.levelRow}>
                <span className={styles.levelLabel}>Nivå</span>
                <span className={styles.levelBadge}>{currentLevelOrder}</span>
              </div>
            )}
          </div>
        ) : (
          gameStops.length > 0 && (
            <p className={styles.classroom}>Ingen fremgang ennå – start eventyret!</p>
          )
        )}

        <button className={styles.editBtn} onClick={() => navigate("/avatar")}>
          Rediger avatar
        </button>
      </div>
    </div>
  );
}

export default ProfilePage;