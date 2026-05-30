import { Table, Card, Input, Tag, Space } from 'antd';
import { useEffect, useState } from 'react';
import { customerApi } from '../services/api';

const CustomerManagement = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [keyword, setKeyword] = useState('');

  useEffect(() => {
    loadCustomers();
  }, [keyword]);

  const loadCustomers = async () => {
    setLoading(true);
    try {
      const response = await customerApi.getCustomerList(keyword || null);
      setCustomers(response.data.data || []);
    } catch (error) {
      console.error('加载客户列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusLabel = (status) => {
    const labels = { 'normal': '正常', 'disabled': '已禁用' };
    return labels[status] || status;
  };

  const columns = [
    {
      title: '客户名称',
      dataIndex: 'customerName',
      key: 'customerName',
    },
    {
      title: '联系人',
      dataIndex: 'contactPerson',
      key: 'contactPerson',
    },
    {
      title: '联系电话',
      dataIndex: 'contactPhone',
      key: 'contactPhone',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        <Tag color={status === 'normal' ? 'green' : 'red'}>
          {getStatusLabel(status)}
        </Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text) => text ? new Date(text).toLocaleString('zh-CN') : '-',
    },
  ];

  return (
    <Card
      title="客户管理"
      bordered={false}
      extra={
        <Space>
          <Input.Search
            placeholder="搜索客户名称 / 联系电话"
            onSearch={(value) => setKeyword(value)}
            onChange={(e) => !e.target.value && setKeyword('')}
            style={{ width: 250 }}
            allowClear
          />
        </Space>
      }
    >
      <Table
        columns={columns}
        dataSource={customers}
        rowKey="customerId"
        loading={loading}
        pagination={{ pageSize: 10 }}
      />
    </Card>
  );
};

export default CustomerManagement;
