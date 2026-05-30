import { Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { selectIsAuthenticated } from './store/slices/authSlice';
import Login from './pages/Login';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import CustomerManagement from './pages/CustomerManagement';
import OrderManagement from './pages/OrderManagement';
import SalesFollowup from './pages/SalesFollowup';
import EquipmentManagement from './pages/EquipmentManagement';

const PrivateRoute = ({ children }) => {
  const isAuthenticated = useSelector(selectIsAuthenticated);
  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <PrivateRoute>
            <Layout />
          </PrivateRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="customers" element={<CustomerManagement />} />
        <Route path="orders" element={<OrderManagement />} />
        <Route path="sales" element={<SalesFollowup />} />
        <Route path="equipment" element={<EquipmentManagement />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default App;
