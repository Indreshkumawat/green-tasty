import React from "react";
import { Box, Button, Grid, Typography, useTheme } from "@mui/material";
import BannerImage from "../assets/ViewBanner.jpg";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
const ViewBannerImage: React.FC = () => {
  const theme = useTheme();
  const { t } = useTranslation();
  const navigate = useNavigate();

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
            variant="body1"
            sx={{ maxWidth: "25%", mt: 2, fontSize: "1.1rem" }}
          >
            {t(
              "A network of restaurants in Tbilisi, Georgia, offering fresh, locally sourced dishes with a focus on health and sustainability."
            )}
          </Typography>
          <Typography
            variant="body1"
            sx={{ maxWidth: "25%", mt: 1, fontSize: "1.1rem" }}
          >
            {t(
              "Our diverse menu includes vegetarian and vegan options, crafted to highlight the rich flavors of Georgian cuisine with a modern twist."
            )}
          </Typography>
          <Box
            sx={{
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              m: 2,
              width: "23%",
            }}
          >
            <Button
              variant="contained"
              color="primary"
              fullWidth
              onClick={() =>
                navigate("/view-menu", { state: { static: true } })
              }
            >
              {t("view_menu")}
            </Button>
          </Box>
        </Box>
      </Box>
    </Grid>
  );
};

export default ViewBannerImage;
