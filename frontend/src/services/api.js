import axios from 'axios';
import { message } from 'antd';
import store from '../store';
import { clearCredentials } from '../store/slices/authSlice';

const MAX_RETRIES = 2;

const createAxiosInstance = () => {
  const instance = axios.create({
    baseURL: '/api',
    timeout: 30000,
  });

  instance.interceptors.request.use(
    (config) => {
      const token = store.getState().auth.token;
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  instance.interceptors.response.use(
    (response) => {
      return response;
    },
    async (error) => {
      const originalRequest = error.config;

      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true;
        store.dispatch(clearCredentials());
        message.error('登录已过期，请重新登录');
        window.location.href = '/login';
        return Promise.reject(error);
      }

      if (error.response?.status === 403) {
        message.error('权限不足');
        return Promise.reject(error);
      }

      if (originalRequest._retryCount === undefined) {
        originalRequest._retryCount = 0;
      }

      if (originalRequest._retryCount < MAX_RETRIES && originalRequest.method !== 'get') {
        originalRequest._retryCount += 1;
        return instance(originalRequest);
      }

      const errorMsg = error.response?.data?.message || error.message || '请求失败';
      if (error.config.showError !== false) {
        message.error(errorMsg);
      }

      return Promise.reject(error);
    }
  );

  return instance;
};

const api = createAxiosInstance();

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  getCurrentUser: () => api.get('/auth/current'),
};

export const orderApi = {
  getOrderList: (params) => api.get('/orders', { params, showError: false }),
  getOrderDetail: (id) => api.get(`/orders/${id}`, { showError: false }),
  createOrder: (data) => api.post('/orders', data),
  updateOrder: (id, data) => api.put(`/orders/${id}`, data),
  updateOrderStatus: (id, status) => api.put(`/orders/${id}/status`, null, { params: { status } }),
  deleteOrders: (ids) => api.delete('/orders', { data: ids }),
};

export const customerApi = {
  getCustomerList: (keyword) => api.get('/customers', { params: { keyword }, showError: false }),
};

export const equipmentApi = {
  getEquipmentList: (status) => api.get('/equipment', { params: { status }, showError: false }),
};

export default api;
