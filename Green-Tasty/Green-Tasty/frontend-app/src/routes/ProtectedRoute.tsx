import { Navigate } from "react-router-dom";
//import Cookies from "js-cookie";

const ProtectedRoute = ({
  children,
  allowedRoles,
}: {
  children: React.ReactNode;
  allowedRoles?: string[];
}) => {
  const isAuthenticated = localStorage.getItem("authToken") !== null;
  const user = localStorage.getItem("role") || "";

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user)) {
    return <Navigate to="/home" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
