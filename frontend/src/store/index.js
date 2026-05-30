import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import orderReducer from './slices/orderSlice';
import uiReducer from './slices/uiSlice';

const loadState = () => {
  try {
    const serializedState = localStorage.getItem('orderManagerState');
    if (serializedState === null) {
      return undefined;
    }
    return JSON.parse(serializedState);
  } catch (err) {
    return undefined;
  }
};

const saveState = (state) => {
  try {
    const serializedState = JSON.stringify({
      auth: state.auth,
    });
    localStorage.setItem('orderManagerState', serializedState);
  } catch (err) {
    // Ignore write errors
  }
};

const preloadedState = loadState();

const store = configureStore({
  reducer: {
    auth: authReducer,
    order: orderReducer,
    ui: uiReducer,
  },
  preloadedState,
});

store.subscribe(() => {
  saveState(store.getState());
});

export default store;
