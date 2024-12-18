// src/services/AuthApi.js
import { api, getAuthHeaders } from './Api';
import { setAccessToken, clearAccessToken } from './TokenManager';
import { handleApiError } from '../utils/Utils';

// 회원가입 API
export const register = async (email, username, password) => {
  try {
    const response = await api.post('/auth/register', { email, username, password });
    return response.data.message;
  } catch (error) {
    throw handleApiError(error, "회원가입 중 오류 발생");
  }
};

// 로그인 API
export const login = async (email, password) => {
  try {
    const response = await api.post('/auth/authenticate', { email, password });
    const { accessToken } = response.data;
    setAccessToken(accessToken);
    return { success: !!accessToken, message: '로그인 성공' };
  } catch (error) {
    throw handleApiError(error, '로그인 실패');
  }
};

// 로그아웃 API
export const logout = async () => {
  try {
    const response = await api.post('/auth/logout', { refreshToken: true }, { headers: getAuthHeaders() });
    clearAccessToken();
    return { success: true, message: response.data.message };
  } catch (error) {
    return { success: false, message: handleApiError(error, '로그아웃 요청이 실패했습니다.').message };
  }
};

// 회원탈퇴 API
export const deleteAccount = async () => {
  try {
    const response = await api.delete('/auth/delete-account', {
      headers: getAuthHeaders(),
    });

    return { success: true, message: response.data.message };
  } catch (error) {
    return { success: false, message: handleApiError(error, "회원탈퇴 요청이 실패했습니다.").message };
  }
};
