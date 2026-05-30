import { Card, Row, Col, Statistic } from 'antd';
import { DollarOutlined, ShoppingCartOutlined, CheckCircleOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { useEffect, useState } from 'react';
import { orderApi } from '../services/api';

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalOrders: 0,
    pendingOrders: 0,
    completedOrders: 0,
    totalAmount: 0,
  });

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const [allResponse, pendingResponse] = await Promise.all([
        orderApi.getOrderList({ page: 1, pageSize: 1000 }),
        orderApi.getOrderList({ page: 1, pageSize: 1000, orderStatus: 'pending' }),
      ]);

      const allOrders = allResponse.data.data.records || [];
      const pendingOrders = pendingResponse.data.data.records || [];

      const totalAmount = allOrders.reduce((sum, order) => {
        return sum + (parseFloat(order.orderAmount) || 0);
      }, 0);

      setStats({
        totalOrders: allResponse.data.data.total || 0,
        pendingOrders: pendingOrders.length,
        completedOrders: allOrders.filter(o => o.orderStatus === 'completed').length,
        totalAmount,
      });
    } catch (error) {
      console.error('加载统计数据失败:', error);
    }
  };

  return (
    <div>
      <h1 style={{ marginBottom: 24 }}>数据概览</h1>
      <Row gutter={16}>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false} style={{ marginBottom: 16 }}>
            <Statistic
              title="订单总数"
              value={stats.totalOrders}
              prefix={<ShoppingCartOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false} style={{ marginBottom: 16 }}>
            <Statistic
              title="待审核"
              value={stats.pendingOrders}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false} style={{ marginBottom: 16 }}>
            <Statistic
              title="已完成"
              value={stats.completedOrders}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false} style={{ marginBottom: 16 }}>
            <Statistic
              title="订单总金额"
              value={stats.totalAmount.toFixed(2)}
              prefix={<DollarOutlined />}
              valueStyle={{ color: '#722ed1' }}
              suffix="元"
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginTop: 24 }}>
        <Col span={24}>
          <Card title="欢迎使用订单管理系统" bordered={false}>
            <p>使用左侧菜单可在不同模块之间切换</p>
            <p>订单管理支持新增、编辑、审核、删除操作</p>
            <p>管理员拥有全部权限，普通操作员仅可查看和创建订单</p>
            <p>订单数据实时更新，支持多条件筛选和搜索</p>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
