import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useClassroom } from './useTeacherQueries';
import { useDeleteClassroom, useUpdateClassroom } from './useTeacherMutations';

export function useClassroomEdit(classroomId: string) {
    const navigate = useNavigate();
    const classroomQuery = useClassroom(classroomId);
    const classroom = classroomQuery.data ?? null;

    const [editMode, setEditMode] = useState(false);
    const [editTitle, setEditTitle] = useState('');
    const [saveError, setSaveError] = useState<string | null>(null);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [didCopyCode, setDidCopyCode] = useState(false);

    const updateMutation = useUpdateClassroom(classroomId);
    const deleteMutation = useDeleteClassroom(classroomId);

    async function handleSave() {
        if (!classroom) return;
        const trimmed = editTitle.trim();
        if (trimmed.length < 2) {
            setSaveError('Klassenavn må være minst 2 tegn.');
            return;
        }
        setSaveError(null);
        try {
            await updateMutation.mutateAsync({ title: trimmed, description: classroom.description });
            setEditMode(false);
        } catch {
            setSaveError('Kunne ikke lagre. Prøv igjen.');
        }
    }

    function handleCancel() {
        if (!classroom) return;
        setEditTitle(classroom.title);
        setSaveError(null);
        setEditMode(false);
    }

    async function handleDelete() {
        try {
            await deleteMutation.mutateAsync();
            navigate('/teacher');
        } catch {
            // error surfaced via deleteError
        }
    }

    async function handleCopyJoinCode() {
        if (!classroom) return;
        try {
            await navigator.clipboard.writeText(classroom.joinCode);
            setDidCopyCode(true);
            window.setTimeout(() => setDidCopyCode(false), 1800);
        } catch {
            setDidCopyCode(false);
        }
    }

    return {
        classroom,
        isLoading: classroomQuery.isLoading,
        editMode,
        setEditMode,
        editTitle,
        setEditTitle,
        saving: updateMutation.isPending,
        saveError,
        handleSave,
        handleCancel,
        showDeleteModal,
        setShowDeleteModal,
        deleting: deleteMutation.isPending,
        deleteError: deleteMutation.isError ? 'Kunne ikke slette. Prøv igjen.' : null,
        handleDelete,
        didCopyCode,
        handleCopyJoinCode,
    };
}
