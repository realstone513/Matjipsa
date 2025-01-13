// src/pages/LoginPage.tsx

import React, { useState } from 'react';
import { useAuthContext } from '../contexts/AuthContext';
import GlobalBackground from '../components/templates/GlobalBackground';
import BackButton from '../components/molecules/BackButton';
import Button from '../components/atoms/Button';
import logo from '../assets/matjipsa_logo.webp';
import title from '../assets/matjipsa_title.webp';

const LoginPage: React.FC = () => {
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string>('');
  const { login } = useAuthContext();

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(email, password);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <GlobalBackground>
      {/* 로고와 타이틀 구역 */}
      <div
        className="d-flex align-items-center justify-content-center mb-4"
        style={{
          marginTop: '5rem', // 상단 여백
        }}
      >
        <img
          src={logo}
          alt="맛집사 로고"
          style={{
            width: '100px',
            height: '100px',
            marginRight: '1rem',
          }}
        />
        <img
          src={title}
          alt="맛집사 타이틀"
          style={{
            width: '200px',
            height: 'auto',
          }}
        />
      </div>

      <form onSubmit={onSubmit}>
        <div className="mb-3">
          <label className="form-label">이메일</label>
          <input
            type="email"
            className="form-control"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={loading}
          />
        </div>
        <div className="mb-3">
          <label className="form-label">비밀번호</label>
          <input
            type="password"
            className="form-control"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={loading}
          />
        </div>
        {error && <div className="text-danger mt-2">{error}</div>}
        <div className="d-flex justify-content-between mt-3">
          <BackButton className="btn btn-light" />
          <Button type="submit" className="btn w-50 ms-2" variant="primary" disabled={loading}>
            {loading ? '로그인 중...' : '로그인'}
          </Button>
        </div>
      </form>
    </GlobalBackground>
  );
};

export default LoginPage;
