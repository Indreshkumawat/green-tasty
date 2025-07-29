import React, {
  useState,
  useReducer,
  useEffect,
  type MouseEvent,
  useCallback,
} from "react";
import {
  styled,
  Toolbar,
  Box,
  IconButton,
  Avatar,
  Tabs,
  Button,
  Tab,
  Grid,
} from "@mui/material";
import MuiAppBar from "@mui/material/AppBar";
import Brightness4Icon from "@mui/icons-material/Brightness4";
import Brightness7Icon from "@mui/icons-material/Brightness7";
//import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import TranslateIcon from "@mui/icons-material/Translate";
//import InfoIcon from "@mui/icons-material/Info";
import { useThemeContext } from "../context/ThemeContextProvider";
import { useLocation, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
//import Cookies from "js-cookie";
import CartContainer from "./cart/CartContainer";
import ProfileTooltip from "./ProfileTooltip";
import LanguageSelector from "./languageSelector";
import AlertComp from "./Alert";

interface CustomAppBarProps {
  positioning?: "fixed" | "absolute" | "sticky" | "static" | "relative";
  logo: string;
  height?: string;
}

const AppBar = styled(MuiAppBar)<{ color: string; height?: string }>(
  ({ color, height }) => ({
    backgroundColor: color,
    height: height,
  })
);

type NavigationState = {
  activeTab: number;
};

type NavigationAction = {
  type: "SET_TAB";
  payload: number;
};

const navigationReducer = (
  state: NavigationState,
  action: NavigationAction
): NavigationState => {
  switch (action.type) {
    case "SET_TAB":
      return { ...state, activeTab: action.payload };
    default:
      return state;
  }
};

const CustomAppBar: React.FC<CustomAppBarProps> = React.memo(
  ({ positioning = "fixed", logo, height = "4rem" }) => {
    console.log("AppBar rendered");
    const location = useLocation();
    const { t } = useTranslation();
    const { toggleTheme, mode } = useThemeContext();

    // const isAuthenticated = !!Cookies.get("authToken");
    // const role = Cookies.get("role") || "";
    // const userName = Cookies.get("username") || "U";
    const isAuthenticated = localStorage.getItem("authToken") !== null;
    const role = localStorage.getItem("role") || "";
    const userName = localStorage.getItem("username") || "U";

    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const [openLanguageSelector, setOpenLanguageSelector] = useState(false);
    const [openCart, setOpenCart] = useState(false);

    const getInitialTab = useCallback(() => {
      const path = location.pathname;
      if (isAuthenticated) {
        if (role === "client") {
          if (path === "/home") return 0;
          if (path === "/reservations") return 1;
          if (path === "/tables") return 2;
        } else if (role === "waiter") {
          if (path === "/reservations") return 0;
        } else if (role === "admin") {
          if (path === "/admin") return 0;
        }
      } else {
        if (path === "/home") return 0;
        if (path === "/tables") return 1;
      }
      return 0;
    }, [location.pathname, isAuthenticated, role]);

    const [state, dispatch] = useReducer(navigationReducer, {
      activeTab: getInitialTab(),
    });

    useEffect(() => {
      const currentTab = getInitialTab();
      if (state.activeTab !== currentTab) {
        dispatch({ type: "SET_TAB", payload: currentTab });
      }
    }, [getInitialTab, state.activeTab]);

    const handleTabChange = (
      _event: React.SyntheticEvent,
      newValue: number
    ) => {
      dispatch({ type: "SET_TAB", payload: newValue });
    };

    const handleClick = (event: MouseEvent<HTMLButtonElement>) => {
      setAnchorEl(event.currentTarget);
    };

    const handleClose = () => {
      setAnchorEl(null);
    };

    return (
      <Grid container>
        <Grid size={{ xs: 12, md: 12 }}>
          <AppBar
            position={positioning}
            color={mode === "light" ? "default" : "primary"}
            height={height}
          >
            <Toolbar>
              <Box sx={{ p: { xs: 0, md: 4 } }}>
                <img
                  src={logo}
                  alt="Logo"
                  style={{
                    height: "2rem",
                    filter: mode === "dark" ? "invert(1)" : "none",
                  }}
                />
              </Box>

              <Tabs
                value={state.activeTab}
                onChange={handleTabChange}
                textColor="primary"
                indicatorColor="primary"
                sx={{ flexGrow: 1 }}
              >
                {(role === "client" || !isAuthenticated) && (
                  <Tab
                    component={Link}
                    to="/home"
                    label={t("main_page")}
                    sx={{ fontSize: "1.1rem" }}
                  />
                )}
                {(role === "client" || role === "waiter") &&
                  isAuthenticated && (
                    <Tab
                      component={Link}
                      to="/reservations"
                      label={t("reservations")}
                      sx={{ fontSize: "1.1rem" }}
                    />
                  )}
                {(role === "client" || !isAuthenticated) && (
                  <Tab
                    component={Link}
                    to="/tables"
                    label={t("book_table")}
                    sx={{ fontSize: "1.1rem" }}
                  />
                )}
              </Tabs>

              {/* <IconButton
                sx={{ color: "primary.main" }}
                onClick={() => {
                  setOpenCart(true);
                }}
              >
                <ShoppingCartIcon fontSize="large" />
              </IconButton> */}

              {/* <IconButton
                sx={{ color: "primary.main" }}
                href=""
                target="_blank"
              >
                <InfoIcon fontSize="large" />
              </IconButton> */}

              <IconButton sx={{ color: "primary.main" }} onClick={toggleTheme}>
                {mode === "light" ? (
                  <Brightness7Icon fontSize="large" />
                ) : (
                  <Brightness4Icon fontSize="large" />
                )}
              </IconButton>

              <IconButton
                sx={{ color: "primary.main" }}
                onClick={() => setOpenLanguageSelector(true)}
              >
                <TranslateIcon fontSize="large" />
              </IconButton>

              {isAuthenticated ? (
                <IconButton
                  onClick={handleClick}
                  sx={{ color: "primary.main" }}
                >
                  <Avatar sx={{ bgcolor: "primary.main" }}>
                    {userName.charAt(0).toUpperCase()}
                  </Avatar>
                </IconButton>
              ) : (
                <Link to="/login" style={{ textDecoration: "none" }}>
                  <Button
                    variant="contained"
                    sx={{ borderRadius: "8px", textTransform: "none" }}
                  >
                    {t("log_in")}
                  </Button>
                </Link>
              )}
            </Toolbar>

            <ProfileTooltip
              handleClose={handleClose}
              anchorEl={anchorEl}
              open={Boolean(anchorEl)}
              setAnchorEl={setAnchorEl}
            />
          </AppBar>

          <Toolbar />
        </Grid>

        {["/reservations", "/my-account"].includes(location.pathname) && (
          <Grid size={{ xs: 12, md: 12 }}>
            <AlertComp />
          </Grid>
        )}

        <LanguageSelector
          open={openLanguageSelector}
          setOpen={setOpenLanguageSelector}
        />
        <CartContainer open={openCart} setOpen={setOpenCart} />
      </Grid>
    );
  }
);

export default CustomAppBar;
