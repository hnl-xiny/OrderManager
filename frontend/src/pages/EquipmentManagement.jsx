import { Card, Row, Col, Statistic } from 'antd';
import { ToolOutlined, CheckCircleOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { useEffect, useState } from 'react';
import { equipmentApi } from '../services/api';

const EquipmentManagement = () => {
  const [equipment, setEquipment] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadEquipment();
  }, []);

  const loadEquipment = async () => {
    setLoading(true);
    try {
      const response = await equipmentApi.getEquipmentList();
      setEquipment(response.data.data || []);
    } catch (error) {
      console.error('加载设备列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const normalCount = equipment.filter(e => e.status === 'normal').length;
  const repairCount = equipment.filter(e => e.status === 'repair').length;
  const disabledCount = equipment.filter(e => e.status === 'disabled').length;

  const getStatusColor = (status) => {
    const colors = {
      'normal': '#52c41a',
      'repair': '#faad14',
      'disabled': '#ff4d4f',
    };
    return colors[status] || '#999';
  };

  const getStatusLabel = (status) => {
    const labels = {
      'normal': '正常',
      'repair': '维修中',
      'disabled': '已停用',
    };
    return labels[status] || status;
  };

  return (
    <div>
      <h1 style={{ marginBottom: 24 }}>设备管理</h1>
      <Row gutter={16}>
        <Col xs={24} sm={8}>
          <Card bordered={false}>
            <Statistic
              title="正常"
              value={normalCount}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false}>
            <Statistic
              title="维修中"
              value={repairCount}
              prefix={<ExclamationCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false}>
            <Statistic
              title="已停用"
              value={disabledCount}
              prefix={<ToolOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        {equipment.map((eq) => (
          <Col xs={24} sm={12} lg={8} key={eq.equipmentId}>
            <Card
              bordered={false}
              loading={loading}
              style={{
                borderLeft: `4px solid ${getStatusColor(eq.status)}`
              }}
            >
              <h3>{eq.equipmentName}</h3>
              <p><strong>编号:</strong> {eq.equipmentCode}</p>
              <p><strong>规格:</strong> {eq.specification || '-'}</p>
              <p><strong>状态:</strong> {getStatusLabel(eq.status)}</p>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
};

export default EquipmentManagement;
