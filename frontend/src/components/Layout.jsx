import { useState } from 'react';
import { useNavigate, Outlet, useLocation } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Layout, Menu, Dropdown, Avatar, Button, message } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  HomeOutlined,
  UserOutlined,
  ShoppingCartOutlined,
  RiseOutlined,
  ToolOutlined,
  LogoutOutlined,
} from '@ant-design/icons';
import { selectUserRole, clearCredentials } from '../store/slices/authSlice';
import { authApi } from '../services/api';

const { Header, Sider, Content } = Layout;

const menuItems = [
  { key: 'dashboard', icon: <HomeOutlined />, label: '数据概览' },
  { key: 'customers', icon: <UserOutlined />, label: '客户管理' },
  { key: 'orders', icon: <ShoppingCartOutlined />, label: '订单管理' },
  { key: 'sales', icon: <RiseOutlined />, label: '销售跟进' },
  { key: 'equipment', icon: <ToolOutlined />, label: '设备管理' },
];

const LayoutComponent = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();
  const role = useSelector(selectUserRole);

  const selectedKey = location.pathname.slice(1) || 'dashboard';

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch (error) {
      // ignore
    }
    dispatch(clearCredentials());
    message.success('已退出登录');
    navigate('/login');
  };

  const userMenu = {
    items: [
      {
        key: 'role',
        label: `角色: ${role || 'unknown'}`,
        disabled: true,
      },
      { type: 'divider' },
      {
        key: 'logout',
        icon: <LogoutOutlined />,
        label: '退出登录',
        onClick: handleLogout,
      },
    ],
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        width={220}
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
        }}
      >
        <div style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#fff',
          fontSize: collapsed ? 16 : 18,
          fontWeight: 'bold',
          borderBottom: '1px solid rgba(255,255,255,0.1)',
        }}>
          {collapsed ? 'OM' : '订单管理系统'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selectedKey]}
          items={menuItems}
          onClick={({ key }) => navigate(`/${key}`)}
          style={{ marginTop: 8 }}
        />
      </Sider>
      <Layout style={{ marginLeft: collapsed ? 80 : 220, transition: 'margin-left 0.2s' }}>
        <Header style={{
          padding: '0 24px',
          background: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 1px 4px rgba(0,0,0,0.1)',
          position: 'sticky',
          top: 0,
          zIndex: 100,
        }}>
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ fontSize: 16, width: 64, height: 64 }}
          />
          <Dropdown menu={userMenu} placement="bottomRight">
            <Avatar style={{ cursor: 'pointer', background: '#1890ff' }}>
              {role === 'admin' ? 'A' : 'U'}
            </Avatar>
          </Dropdown>
        </Header>
        <Content style={{ margin: '24px 16px', minHeight: 280 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default LayoutComponent;
