import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  token: localStorage.getItem('token') || null,
  userInfo: null,
  role: null,
  isAuthenticated: false,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials: (state, action) => {
      const { token, username, role } = action.payload;
      state.token = token;
      state.userInfo = { username };
      state.role = role;
      state.isAuthenticated = true;
      localStorage.setItem('token', token);
    },
    clearCredentials: (state) => {
      state.token = null;
      state.userInfo = null;
      state.role = null;
      state.isAuthenticated = false;
      localStorage.removeItem('token');
    },
    setUserInfo: (state, action) => {
      state.userInfo = action.payload;
    },
  },
});

export const { setCredentials, clearCredentials, setUserInfo } = authSlice.actions;

export const selectAuth = (state) => state.auth;
export const selectIsAuthenticated = (state) => state.auth.isAuthenticated;
export const selectUserRole = (state) => state.auth.role;
export const selectToken = (state) => state.auth.token;

export default authSlice.reducer;
