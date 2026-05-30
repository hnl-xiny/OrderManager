import { Card, Timeline, Tag } from 'antd';
import { useEffect, useState } from 'react';
import { orderApi } from '../services/api';
import dayjs from 'dayjs';

const SalesFollowup = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    setLoading(true);
    try {
      const response = await orderApi.getOrderList({ page: 1, pageSize: 20 });
      const records = response.data.data?.records || response.data.data || [];
      const recentOrders = records.slice(0, 10);
      setOrders(recentOrders);
    } catch (error) {
      console.error('加载订单列表失败:', error);
    } finally {
      setLoading(false);
    }
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

  return (
    <Card title="销售跟进" bordered={false} loading={loading}>
      <Timeline
        items={orders.map((order) => ({
          color: order.orderStatus === 'completed' ? 'green' :
                 order.orderStatus === 'shipped' ? 'blue' : 'gray',
          children: (
            <div style={{ paddingBottom: 8 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <strong>{order.customerName}</strong>
                <Tag color={getStatusColor(order.orderStatus)}>{getStatusLabel(order.orderStatus)}</Tag>
              </div>
              <div style={{ color: '#666', fontSize: 12, marginTop: 4 }}>
                <p>设备: {order.equipmentName}</p>
                <p>金额: ¥{order.orderAmount}</p>
                <p>交付日期: {order.deliveryDate}</p>
                <p>创建时间: {dayjs(order.createdAt).format('YYYY-MM-DD HH:mm')}</p>
              </div>
            </div>
          ),
        }))}
      />
    </Card>
  );
};

export default SalesFollowup;
