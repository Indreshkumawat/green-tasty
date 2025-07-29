import { Box, CircularProgress } from "@mui/material";
import AppRoutes from "./routes/Routes.tsx";
import ThemeContextProvider from "./context/ThemeContextProvider.tsx";

import React from "react";
import { SnackbarProvider } from "notistack";
import { Provider } from "react-redux";
import { PersistGate } from "redux-persist/integration/react";
import { store, persistor } from "./redux/store";

//import UpdateProfile from "./components/UpdateProfile";

const LoadingComponent = () => (
  <Box
    sx={{
      display: "flex",
      justifyContent: "center",
      alignItems: "center",
      height: "100vh",
    }}
  >
    <CircularProgress />
  </Box>
);

const App: React.FC = () => {
  return (
    <ThemeContextProvider>
      <Provider store={store}>
        <PersistGate loading={<LoadingComponent />} persistor={persistor}>
          <SnackbarProvider
            maxSnack={3}
            anchorOrigin={{
              vertical: "top",
              horizontal: "center",
            }}
            autoHideDuration={3000}
          >
            <Box
              sx={{
                m: 0,
                height: "100%",
                width: "100%",
                overflowX: "hidden",
              }}
            >
              <AppRoutes />
            </Box>
          </SnackbarProvider>
        </PersistGate>
      </Provider>
    </ThemeContextProvider>
  );
};

export default App;
