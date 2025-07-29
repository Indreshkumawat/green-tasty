import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

import HomePage from "../pages/homePage.tsx";
import TablePage from "../pages/tablePage.tsx";
import LoginPage from "../pages/loginPage.tsx";
import RegisterPage from "../pages/registerPage.tsx";
import ProtectedRoute from "./ProtectedRoute.tsx";

import RestaurantProfile from "../pages/RestaurantProfile.tsx";
//import Cookies from "js-cookie";
import MyAccount from "../pages/accountMainPage.tsx";
import MainPage from "../pages/mainPage.tsx";
import ReservationPage from "../pages/reservationPage.tsx";
import ViewMenu from "../pages/viewMenu.tsx";
import AdminPage from "../pages/adminPage.tsx";

function AppRoutes(): React.ReactNode {
  return (
    <Router>
      <AppRoutesContent />
    </Router>
  );
}

function AppRoutesContent(): React.ReactNode {
  const isAuthenticated = localStorage.getItem("authToken") !== null;
  const role = localStorage.getItem("role") || "";

  const getDefaultRoute = () => {
    if (role === "admin") return "/reservations";
    if (role === "waiter") return "/reservations";
    if (role === "client") return "/reservations";
    return "/home";
  };

  return (
    <Routes>
      {/* ✅ Auth Routes (No AppBar) */}
      <Route
        path="/login"
        element={
          isAuthenticated ? (
            <Navigate to={getDefaultRoute()} replace />
          ) : (
            <LoginPage />
          )
        }
      />
      <Route
        path="/register"
        element={
          isAuthenticated ? (
            <Navigate to={getDefaultRoute()} replace />
          ) : (
            <RegisterPage />
          )
        }
      />

      {/* ✅ All other routes use MainPage (with AppBar) */}
      <Route element={<MainPage />}>
        {/* Redirect from "/" based on role */}
        <Route path="/" element={<Navigate to={getDefaultRoute()} replace />} />

        {/* Public Pages (with AppBar) */}
        <Route path="/home" element={<HomePage />} />
        <Route path="/tables" element={<TablePage />} />
        <Route path="/view-menu" element={<ViewMenu />} />

        {/* Protected Pages */}
        <Route
          path="/reservations"
          element={
            <ProtectedRoute allowedRoles={["waiter", "client", "admin"]}>
              <ReservationPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-account"
          element={
            <ProtectedRoute allowedRoles={["waiter", "admin", "client"]}>
              <MyAccount />
            </ProtectedRoute>
          }
        />
        <Route
          path="/restaurant-profile"
          element={
            <ProtectedRoute allowedRoles={["client"]}>
              <RestaurantProfile />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={["admin"]}>
              <AdminPage />
            </ProtectedRoute>
          }
        />
      </Route>

      {/* 404 fallback */}
      <Route path="*" element={<Navigate to="/home" />} />
    </Routes>
  );
}

export default AppRoutes;
