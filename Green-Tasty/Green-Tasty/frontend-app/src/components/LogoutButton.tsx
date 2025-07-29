import { Box, Button } from "@mui/material";
import React from "react";
import { useNavigate } from "react-router-dom";
//import Cookies from "js-cookie";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import { clearCart } from "../redux/cartSlice";
import type { AppDispatch } from "../redux/store";

interface LogoutProps {
  padding: number | string | [number, number, number, number];
  handleClose?: () => void;
  setAnchorEl?: (element: HTMLElement | null) => void;
  width?: string;
}

const Logout: React.FC<LogoutProps> = ({
  padding,
  handleClose: externalHandleClose,
  setAnchorEl,
  width,
}) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();

  const handleClose = (): void => {
    // Clear Redux store
    dispatch(clearCart());

    // Clear cookies
    // Cookies.remove("authToken");
    // Cookies.remove("role");
    // Cookies.remove("username");
    localStorage.removeItem("authToken");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
    // Clear persisted data from localStorage
    localStorage.removeItem("persist:root");

    // Navigate and reload
    navigate("/home");
    window.location.reload();

    externalHandleClose?.();
    setAnchorEl?.(null);
  };

  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        p: padding,
        width: width ? width : "100%",
      }}
      onClick={handleClose}
    >
      <Button
        variant="outlined"
        fullWidth
        color="error"
        sx={{ borderRadius: "10px" }}
      >
        {t("Log Out")}
      </Button>
    </Box>
  );
};

export default Logout;
