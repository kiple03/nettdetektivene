import { Outlet } from "react-router-dom";
import ProfileIconButton from "@/components/student/ProfileIconButton";

export default function StudentLayout() {
    return (
        <>
            <ProfileIconButton />
            <Outlet />
        </>
    );
}
