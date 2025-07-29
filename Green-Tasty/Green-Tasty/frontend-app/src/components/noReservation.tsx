import { Box, Typography, Button } from "@mui/material";
import { useTranslation } from "react-i18next";
import noReservation from "../assets/no_reservation.svg";
import { useNavigate } from "react-router-dom";
//import Cookies from "js-cookie";
function NoReservation() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const role = localStorage.getItem("role") || "";
  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        height: "100%",
        width: "100%",
        gap: 2,
      }}
    >
      <img src={noReservation} alt="no reservation" />
      <Typography variant="h6">{t("no_reservation")}</Typography>
      {role.toLowerCase() === "client" ? (
        <>
          <Typography variant="body1">
            {t("looks_like_you_haven_t_made_any_reservations_yet")}
          </Typography>
          <Button
            variant="contained"
            color="primary"
            sx={{ width: "30%", borderRadius: "8px" }}
            onClick={() => navigate("/tables")}
          >
            {t("book_a_table")}
          </Button>
        </>
      ) : (
        <></>
      )}
    </Box>
  );
}

export default NoReservation;
