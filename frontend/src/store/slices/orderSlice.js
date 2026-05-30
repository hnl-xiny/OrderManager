import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { orderApi } from '../../services/api';

export const fetchOrders = createAsyncThunk(
  'order/fetchOrders',
  async (params, { rejectWithValue }) => {
    try {
      const response = await orderApi.getOrderList(params);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

export const fetchOrderDetail = createAsyncThunk(
  'order/fetchOrderDetail',
  async (orderId, { rejectWithValue }) => {
    try {
      const response = await orderApi.getOrderDetail(orderId);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

export const createOrder = createAsyncThunk(
  'order/createOrder',
  async (orderData, { rejectWithValue }) => {
    try {
      const response = await orderApi.createOrder(orderData);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

export const updateOrder = createAsyncThunk(
  'order/updateOrder',
  async ({ orderId, orderData }, { rejectWithValue }) => {
    try {
      const response = await orderApi.updateOrder(orderId, orderData);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

export const updateOrderStatus = createAsyncThunk(
  'order/updateOrderStatus',
  async ({ orderId, status }, { rejectWithValue }) => {
    try {
      await orderApi.updateOrderStatus(orderId, status);
      return { orderId, status };
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

export const deleteOrders = createAsyncThunk(
  'order/deleteOrders',
  async (orderIds, { rejectWithValue }) => {
    try {
      await orderApi.deleteOrders(orderIds);
      return orderIds;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

const initialState = {
  orders: [],
  currentOrder: null,
  pagination: {
    total: 0,
    current: 1,
    pageSize: 8,
  },
  filters: {
    orderType: null,
    orderStatus: null,
    startDate: null,
    endDate: null,
    keyword: null,
  },
  loading: false,
  error: null,
};

const orderSlice = createSlice({
  name: 'order',
  initialState,
  reducers: {
    setOrders: (state, action) => {
      state.orders = action.payload;
    },
    setFilters: (state, action) => {
      state.filters = { ...state.filters, ...action.payload };
      state.pagination.current = 1;
    },
    updateOrderOptimistic: (state, action) => {
      const { orderId, status } = action.payload;
      const order = state.orders.find(o => o.orderId === orderId);
      if (order) {
        order.orderStatus = status;
      }
    },
    clearFilters: (state) => {
      state.filters = initialState.filters;
      state.pagination.current = 1;
    },
    setPagination: (state, action) => {
      state.pagination = { ...state.pagination, ...action.payload };
    },
    clearCurrentOrder: (state) => {
      state.currentOrder = null;
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // 获取订单列表
      .addCase(fetchOrders.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchOrders.fulfilled, (state, action) => {
        state.loading = false;
        state.orders = action.payload.data.records || action.payload.data;
        state.pagination.total = action.payload.data.total || 0;
      })
      .addCase(fetchOrders.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // 获取订单详情
      .addCase(fetchOrderDetail.fulfilled, (state, action) => {
        state.currentOrder = action.payload.data;
      })
      // 创建订单
      .addCase(createOrder.fulfilled, (state, action) => {
        state.currentOrder = action.payload.data;
      })
      // 更新订单状态
      .addCase(updateOrderStatus.fulfilled, (state, action) => {
        const { orderId, status } = action.payload;
        const order = state.orders.find(o => o.orderId === orderId);
        if (order) {
          order.orderStatus = status;
        }
      })
      // 删除订单
      .addCase(deleteOrders.fulfilled, (state, action) => {
        state.orders = state.orders.filter(
          order => !action.payload.includes(order.orderId)
        );
      });
  },
});

export const { setFilters, clearFilters, setPagination, clearCurrentOrder, clearError, setOrders, updateOrderOptimistic } = orderSlice.actions;

export const selectOrders = (state) => state.order.orders;
export const selectCurrentOrder = (state) => state.order.currentOrder;
export const selectPagination = (state) => state.order.pagination;
export const selectFilters = (state) => state.order.filters;
export const selectOrderLoading = (state) => state.order.loading;
export const selectOrderError = (state) => state.order.error;

export default orderSlice.reducer;
