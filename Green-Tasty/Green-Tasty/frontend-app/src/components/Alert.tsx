import { Alert, Box, Typography } from "@mui/material";
import BaseImage from "../assets/Base.png";
import Platespoon from "../assets/PlateSpoon.svg";

import { useTranslation } from "react-i18next";
//import Cookies from "js-cookie";

const AlertComp = () => {
  const { t } = useTranslation();
  const username = localStorage.getItem("username") || "";
  return (
    <Box sx={{ position: "relative" }}>
      <Alert
        variant="filled"
        icon={false}
        sx={{
          backgroundImage: `url(${BaseImage})`,
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
          minHeight: "130px", // Adjust as needed
        }}
      >
        <Typography
          variant="h5"
          sx={{
            justifyContent: "center",
            alignItems: "start",
            fontFamily: "lexend",
            p: 4,
          }}
        >
          {`${t("Hello")}${username ? `, ${username}!` : "!"}`}
        </Typography>
        {/* <Typography
          variant="body1"
          sx={{
            justifyContent: "center",
            alignItems: "start",
            fontFamily: "lexend",
            pl: 4,
          }}
        >
          {t(
            "Using free version of Render so the initial load may take 2-3 minutes."
          )}
        </Typography> */}
      </Alert>

      {/* Platespoon logo positioned to the right */}
      <Box
        component="img"
        src={Platespoon}
        alt="Platespoon Logo"
        sx={{
          position: "absolute",
          top: "50%",
          right: 16,
          transform: "translateY(-50%)",
          height: 80, // Adjust height as needed
        }}
      />
    </Box>
  );
};

export default AlertComp;
