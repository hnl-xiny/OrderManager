import { useEffect, useState, useCallback } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import {
  Card, Table, Button, Space, Input, Select, DatePicker, Tag, Modal,
  Form, Popconfirm, message
} from 'antd';
import {
  PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined,
  CheckCircleOutlined, CloseCircleOutlined, EyeOutlined, ReloadOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import {
  fetchOrders, selectOrders, selectPagination, selectFilters,
  selectOrderLoading, selectOrderError, setFilters, clearFilters,
  setPagination, clearError
} from '../store/slices/orderSlice';
import { selectUserRole } from '../store/slices/authSlice';
import { customerApi, equipmentApi } from '../services/api';

const { RangePicker } = DatePicker;

const OrderManagement = () => {
  const dispatch = useDispatch();
  const orders = useSelector(selectOrders);
  const pagination = useSelector(selectPagination);
  const filters = useSelector(selectFilters);
  const loading = useSelector(selectOrderLoading);
  const error = useSelector(selectOrderError);
  const userRole = useSelector(selectUserRole);

  const [form] = Form.useForm();
  const [modalVisible, setModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [currentOrder, setCurrentOrder] = useState(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [equipment, setEquipment] = useState([]);
  const [submitting, setSubmitting] = useState(false);

  const isAdmin = userRole === 'admin';

  useEffect(() => {
    dispatch(fetchOrders({ ...filters, page: pagination.current, pageSize: pagination.pageSize }));
  }, [dispatch, filters, pagination.current, pagination.pageSize]);

  useEffect(() => {
    if (error) {
      message.error(error.message || '操作失败');
      dispatch(clearError());
    }
  }, [error, dispatch]);

  const loadCustomers = useCallback(async () => {
    try {
      const response = await customerApi.getCustomerList();
      setCustomers(response.data.data || []);
    } catch (err) {
      console.error('加载客户列表失败:', err);
    }
  }, []);

  const loadEquipment = useCallback(async () => {
    try {
      const response = await equipmentApi.getEquipmentList('normal');
      setEquipment(response.data.data || []);
    } catch (err) {
      console.error('加载设备列表失败:', err);
    }
  }, []);

  const handleSearch = () => {
    dispatch(fetchOrders({ ...filters, page: 1, pageSize: pagination.pageSize }));
  };

  const handleReset = () => {
    dispatch(clearFilters());
  };

  const handleTableChange = (newPagination) => {
    dispatch(setPagination({ current: newPagination.current, pageSize: newPagination.pageSize }));
  };

  const openAddModal = () => {
    setCurrentOrder(null);
    form.resetFields();
    loadCustomers();
    loadEquipment();
    setModalVisible(true);
  };

  const openEditModal = (record) => {
    setCurrentOrder(record);
    loadCustomers();
    loadEquipment();
    form.setFieldsValue({
      customerId: record.customerId,
      equipmentId: record.equipmentId,
      orderType: record.orderType,
      orderAmount: record.orderAmount,
      deliveryDate: dayjs(record.deliveryDate),
      remarks: record.remarks,
    });
    setModalVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);

      const orderData = {
        ...values,
        deliveryDate: values.deliveryDate.format('YYYY-MM-DD'),
      };

      const { orderApi } = await import('../services/api');

      if (currentOrder) {
        await orderApi.updateOrder(currentOrder.orderId, orderData);
        message.success('订单已更新');
      } else {
        await orderApi.createOrder(orderData);
        message.success('订单已创建');
      }

      setModalVisible(false);
      dispatch(fetchOrders({ ...filters, page: pagination.current, pageSize: pagination.pageSize }));
    } catch (err) {
      if (err.response?.status === 409) {
        Modal.confirm({
          title: '重复订单提示',
          content: err.response?.data?.message || '今日已存在相同客户和设备的订单，是否仍要创建？',
          okText: '仍要创建',
          cancelText: '取消',
          onOk: async () => {
            try {
              const { orderApi } = await import('../services/api');
              const values = await form.validateFields();
              await orderApi.createOrder({
                ...values,
                deliveryDate: values.deliveryDate.format('YYYY-MM-DD'),
                forceCreate: true,
              });
              message.success('订单已创建');
              setModalVisible(false);
              dispatch(fetchOrders({ ...filters, page: pagination.current, pageSize: pagination.pageSize }));
            } catch (retryErr) {
              if (retryErr.response?.data?.message) {
                message.error(retryErr.response.data.message);
              }
            }
          },
        });
      } else if (err.response?.data?.message) {
        message.error(err.response.data.message);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (orderIds) => {
    try {
      const { orderApi } = await import('../services/api');
      await orderApi.deleteOrders(orderIds);
      message.success('删除成功');
      setSelectedRowKeys([]);
      dispatch(fetchOrders({ ...filters, page: pagination.current, pageSize: pagination.pageSize }));
    } catch (err) {
      if (err.response?.data?.message) {
        message.error(err.response.data.message);
      }
    }
  };

  const handleAudit = async (orderId, status) => {
    // 乐观更新：立即在本地状态中更新，不等接口返回
    const prevOrders = [...orders];
    dispatch({
      type: 'order/updateOrderOptimistic',
      payload: { orderId, status },
    });
    message.success(status === 'approved' ? '审核通过' : '已取消审核');
    try {
      const { orderApi } = await import('../services/api');
      await orderApi.updateOrderStatus(orderId, status);
    } catch (err) {
      // 接口失败时回滚
      dispatch({ type: 'order/setOrders', payload: prevOrders });
      if (err.response?.data?.message) {
        message.error(err.response.data.message);
      }
    }
  };

  const openDetailModal = (record) => {
    setCurrentOrder(record);
    setDetailModalVisible(true);
  };

  const getStatusColor = (status) => {
    const colors = {
      'pending': 'orange',
      'approved': 'blue',
      'shipped': 'purple',
      'completed': 'green',
    };
    return colors[status] || 'default';
  };

  const getStatusLabel = (status) => {
    const labels = {
      'pending': '待审核',
      'approved': '已通过',
      'shipped': '已发货',
      'completed': '已完成',
    };
    return labels[status] || status;
  };

  const getTypeLabel = (type) => {
    const labels = { 'purchase': '采购', 'sales': '销售' };
    return labels[type] || type;
  };

  const columns = [
    {
      title: '订单编号',
      dataIndex: 'orderId',
      key: 'orderId',
      width: 220,
      ellipsis: true,
    },
    {
      title: '客户',
      dataIndex: 'customerName',
      key: 'customerName',
      width: 150,
    },
    {
      title: '类型',
      dataIndex: 'orderType',
      key: 'orderType',
      width: 100,
      render: (type) => (
        <Tag color={type === 'purchase' ? 'blue' : 'green'}>
          {getTypeLabel(type)}
        </Tag>
      ),
    },
    {
      title: '金额',
      dataIndex: 'orderAmount',
      key: 'orderAmount',
      width: 120,
      render: (amount) => `¥${parseFloat(amount || 0).toFixed(2)}`,
    },
    {
      title: '交付日期',
      dataIndex: 'deliveryDate',
      key: 'deliveryDate',
      width: 120,
    },
    {
      title: '设备',
      dataIndex: 'equipmentName',
      key: 'equipmentName',
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'orderStatus',
      key: 'orderStatus',
      width: 110,
      render: (status) => (
        <Tag color={getStatusColor(status)}>{getStatusLabel(status)}</Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 160,
      render: (text) => text ? dayjs(text).format('YYYY-MM-DD HH:mm') : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 260,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openDetailModal(record)}>
            详情
          </Button>
          {isAdmin && (
            <>
              {record.orderStatus === 'pending' && (
                <Button type="link" size="small" icon={<CheckCircleOutlined />}
                  onClick={() => handleAudit(record.orderId, 'approved')}>
                  审核
                </Button>
              )}
              {record.orderStatus === 'approved' && (
                <Button type="link" size="small" icon={<CloseCircleOutlined />}
                  onClick={() => handleAudit(record.orderId, 'pending')}>
                  取消审核
                </Button>
              )}
            </>
          )}
          {record.orderStatus === 'pending' && (
            <>
              <Button type="link" size="small" icon={<EditOutlined />}
                onClick={() => openEditModal(record)}>
                编辑
              </Button>
              {isAdmin && (
                <Popconfirm title="确认删除此订单？" onConfirm={() => handleDelete([record.orderId])}
                  okText="确定" cancelText="取消">
                  <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                    删除
                  </Button>
                </Popconfirm>
              )}
            </>
          )}
        </Space>
      ),
    },
  ];

  const rowSelection = isAdmin ? {
    selectedRowKeys,
    onChange: setSelectedRowKeys,
    getCheckboxProps: (record) => ({ disabled: record.orderStatus !== 'pending' }),
  } : undefined;

  return (
    <Card
      title="订单管理"
      bordered={false}
      extra={
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={openAddModal}>
            新建订单
          </Button>
          <Button icon={<ReloadOutlined />} onClick={() => dispatch(fetchOrders({ ...filters, page: pagination.current, pageSize: pagination.pageSize }))}>
            刷新
          </Button>
        </Space>
      }
    >
      <div style={{ marginBottom: 16, padding: 16, background: '#fafafa', borderRadius: 8 }}>
        <Space wrap size="middle">
          <Input
            placeholder="搜索订单编号 / 客户 / 设备"
            style={{ width: 250 }}
            value={filters.keyword}
            onChange={(e) => dispatch(setFilters({ keyword: e.target.value }))}
            onPressEnter={handleSearch}
            allowClear
          />
          <Select
            placeholder="订单类型"
            style={{ width: 140 }}
            allowClear
            value={filters.orderType}
            onChange={(value) => dispatch(setFilters({ orderType: value }))}
          >
            <Select.Option value="purchase">采购</Select.Option>
            <Select.Option value="sales">销售</Select.Option>
          </Select>
          <Select
            placeholder="订单状态"
            style={{ width: 140 }}
            allowClear
            value={filters.orderStatus}
            onChange={(value) => dispatch(setFilters({ orderStatus: value }))}
          >
            <Select.Option value="pending">待审核</Select.Option>
            <Select.Option value="approved">已通过</Select.Option>
            <Select.Option value="shipped">已发货</Select.Option>
            <Select.Option value="completed">已完成</Select.Option>
          </Select>
          <RangePicker
            onChange={(dates) => {
              dispatch(setFilters({
                startDate: dates?.[0]?.format('YYYY-MM-DD'),
                endDate: dates?.[1]?.format('YYYY-MM-DD'),
              }));
            }}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            查询
          </Button>
          <Button onClick={handleReset}>重置</Button>
          {isAdmin && selectedRowKeys.length > 0 && (
            <Popconfirm
              title={`确定删除选中的 ${selectedRowKeys.length} 条订单？`}
              onConfirm={() => handleDelete(selectedRowKeys)}
              okText="确定" cancelText="取消">
              <Button danger icon={<DeleteOutlined />}>
                批量删除 ({selectedRowKeys.length})
              </Button>
            </Popconfirm>
          )}
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={orders}
        rowKey="orderId"
        loading={loading}
        rowSelection={rowSelection}
        pagination={{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
        }}
        onChange={handleTableChange}
        scroll={{ x: 1200 }}
      />

      <Modal
        title={currentOrder ? '编辑订单' : '新建订单'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        confirmLoading={submitting}
        width={600}
        okText="确定" cancelText="取消"
      >
        <Form form={form} layout="vertical" style={{ marginTop: 20 }}>
          <Form.Item
            name="customerId"
            label="客户"
            rules={[{ required: true, message: '请选择客户' }]}
          >
            <Select
              showSearch
              placeholder="请选择客户"
              filterOption={(input, option) =>
                option.children.props.children[1].props.children
                  .toLowerCase()
                  .includes(input.toLowerCase())
              }
            >
              {customers.map((c) => (
                <Select.Option key={c.customerId} value={c.customerId}>
                  {c.customerName} - {c.contactPhone}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="equipmentId"
            label="设备"
            rules={[{ required: true, message: '请选择设备' }]}
          >
            <Select placeholder="请选择设备" showSearch>
              {equipment.map((e) => (
                <Select.Option key={e.equipmentId} value={e.equipmentId}>
                  {e.equipmentName} ({e.equipmentCode})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="orderType"
            label="订单类型"
            rules={[{ required: true, message: '请选择订单类型' }]}
          >
            <Select placeholder="请选择订单类型">
              <Select.Option value="purchase">采购</Select.Option>
              <Select.Option value="sales">销售</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="orderAmount"
            label="订单金额"
            rules={[
              { required: true, message: '请输入金额' },
              { pattern: /^\d+(\.\d{1,2})?$/, message: '金额格式不正确' },
            ]}
          >
            <Input prefix="¥" placeholder="请输入金额" />
          </Form.Item>

          <Form.Item
            name="deliveryDate"
            label="交付日期"
            rules={[{ required: true, message: '请选择交付日期' }]}
          >
            <DatePicker
              style={{ width: '100%' }}
              disabledDate={(current) => current && current < dayjs().startOf('day')}
            />
          </Form.Item>

          <Form.Item name="remarks" label="备注">
            <Input.TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="订单详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={600}
      >
        {currentOrder && (
          <div style={{ padding: '16px 0' }}>
            <p><strong>订单编号:</strong> {currentOrder.orderId}</p>
            <p><strong>客户:</strong> {currentOrder.customerName}</p>
            <p><strong>联系电话:</strong> {currentOrder.customerPhone || '-'}</p>
            <p><strong>设备:</strong> {currentOrder.equipmentName}</p>
            <p><strong>规格:</strong> {currentOrder.equipmentSpec || '-'}</p>
            <p><strong>类型:</strong> {getTypeLabel(currentOrder.orderType)}</p>
            <p><strong>金额:</strong> ¥{parseFloat(currentOrder.orderAmount || 0).toFixed(2)}</p>
            <p><strong>交付日期:</strong> {currentOrder.deliveryDate}</p>
            <p><strong>状态:</strong>
              <Tag color={getStatusColor(currentOrder.orderStatus)} style={{ marginLeft: 8 }}>
                {getStatusLabel(currentOrder.orderStatus)}
              </Tag>
            </p>
            <p><strong>备注:</strong> {currentOrder.remarks || '-'}</p>
            <p><strong>创建时间:</strong> {currentOrder.createdAt ? dayjs(currentOrder.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'}</p>
            <p><strong>更新时间:</strong> {currentOrder.updatedAt ? dayjs(currentOrder.updatedAt).format('YYYY-MM-DD HH:mm:ss') : '-'}</p>
          </div>
        )}
      </Modal>
    </Card>
  );
};

export default OrderManagement;
