import apiClient from "./client";

export interface MedalionResponse{
    id: string,
    name: string,
    description: string,
    earnedAt: string,
    imageUrl: string
}

export const fetchMedalions = async (): Promise<MedalionResponse[]> => {
    const res = await apiClient.get<MedalionResponse[]>('/user/medalions');
    return res.data;
};