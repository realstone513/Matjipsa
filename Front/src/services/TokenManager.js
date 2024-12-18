// src/services/TokenManager.js

let accessToken = null;

export const setAccessToken = (token) => {
  accessToken = token;
};

export const getAccessToken = () => accessToken;

export const clearAccessToken = () => {
  accessToken = null;
};

// Access Token 갱신 함수
export const refreshAccessToken = async (api) => {
  try {
    const response = await api.post('/auth/refresh', {
      refreshToken: true, // Refresh Token은 HttpOnly Cookie에 의해 전달됨
    });
    const newAccessToken = response.data.accessToken;

    // Access Token 갱신
    setAccessToken(newAccessToken);

    return newAccessToken;
  } catch (error) {
    clearAccessToken();
    throw new Error('Access Token 갱신 실패: 사용자 로그인이 필요합니다.');
  }
};
