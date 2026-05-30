import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  sidebarCollapsed: false,
  currentModule: 'dashboard',
  modalVisible: {
    orderForm: false,
    orderDetail: false,
    confirm: false,
  },
  editingOrder: null,
};

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    toggleSidebar: (state) => {
      state.sidebarCollapsed = !state.sidebarCollapsed;
    },
    setSidebarCollapsed: (state, action) => {
      state.sidebarCollapsed = action.payload;
    },
    setCurrentModule: (state, action) => {
      state.currentModule = action.payload;
    },
    openOrderFormModal: (state, action) => {
      state.modalVisible.orderForm = true;
      state.editingOrder = action.payload || null;
    },
    closeOrderFormModal: (state) => {
      state.modalVisible.orderForm = false;
      state.editingOrder = null;
    },
    openOrderDetailModal: (state) => {
      state.modalVisible.orderDetail = true;
    },
    closeOrderDetailModal: (state) => {
      state.modalVisible.orderDetail = false;
    },
    openConfirmModal: (state) => {
      state.modalVisible.confirm = true;
    },
    closeConfirmModal: (state) => {
      state.modalVisible.confirm = false;
    },
  },
});

export const {
  toggleSidebar,
  setSidebarCollapsed,
  setCurrentModule,
  openOrderFormModal,
  closeOrderFormModal,
  openOrderDetailModal,
  closeOrderDetailModal,
  openConfirmModal,
  closeConfirmModal,
} = uiSlice.actions;

export const selectSidebarCollapsed = (state) => state.ui.sidebarCollapsed;
export const selectCurrentModule = (state) => state.ui.currentModule;
export const selectModalVisible = (state) => state.ui.modalVisible;
export const selectEditingOrder = (state) => state.ui.editingOrder;

export default uiSlice.reducer;
