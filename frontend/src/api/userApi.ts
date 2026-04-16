import clientApi from './client';

export const fetchUserProfile = async () => {
    const response = await clientApi.get('/user/profile');
    return response.data;
};