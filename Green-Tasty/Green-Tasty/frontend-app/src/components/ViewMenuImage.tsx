import React from "react";
import { Box, Grid, Typography, useTheme } from "@mui/material";
import BannerImage from "../assets/ViewBanner.jpg";
import { useTranslation } from "react-i18next";
const ViewMenuImage: React.FC = () => {
  const theme = useTheme();
  const { t } = useTranslation();

  return (
    <Grid
      size={{ xs: 12, md: 12, lg: 12 }}
      sx={{ height: "400px", overflow: "hidden", marginTop: -4 }}
    >
      <Box sx={{ position: "relative", width: "100%", height: "100%" }}>
        {/* Background Image */}
        <img
          src={BannerImage}
          alt="Banner"
          style={{
            width: "100%",
            height: "100%",
            objectFit: "cover",
            display: "block",
          }}
        />

        {/* Overlay Text */}
        <Box
          sx={{
            position: "absolute",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            bgcolor: "rgba(0,0,0,0.4)", // optional dark overlay
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            alignItems: "start",
            textAlign: "center",
            color: "white",
            px: 2,
          }}
        >
          <Typography
            variant="h4"
            component="h1"
            sx={{
              fontWeight: "bold",
              color: theme.palette.primary.main,
              justifyContent: "center",
              textAlign: "center",
              alignItems: "center",
              display: "flex",
              pl: 4,
            }}
          >
            {t("green_&_tasty")}
          </Typography>
          <Typography
            variant="h2"
            component="h1"
            sx={{
              fontWeight: "bold",
              color: theme.palette.primary.main,
              justifyContent: "center",
              textAlign: "center",
              alignItems: "center",
              display: "flex",
              pl: 4,
            }}
          >
            {t("menu")}
          </Typography>
        </Box>
      </Box>
    </Grid>
  );
};

export default ViewMenuImage;
