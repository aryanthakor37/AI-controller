import axios from 'axios';
import { getApiUrl } from '../config';
import { store } from '../redux/store';
import { logout } from '../redux/slices/userSlice';

// Configure the base URL for the backend API
const api = axios.create({
  baseURL: getApiUrl(),
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Request interceptor to automatically attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle expired tokens globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // 401 Unauthorized implies JWT expired or invalid
      store.dispatch(logout());
      
      // The DashboardLayout/Auth guard will handle redirect, 
      // but if we are deeply nested or not observing state, reload forces redirect
      if (window.location.pathname.startsWith('/dashboard')) {
        window.location.href = '/login?expired=true';
      }
    }
    return Promise.reject(error);
  }
);

export default api;

