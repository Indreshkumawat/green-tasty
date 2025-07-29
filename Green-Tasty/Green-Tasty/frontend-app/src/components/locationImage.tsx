import React from "react";
import { Box, Grid } from "@mui/material";

const LocationImage: React.FC<{ imageUrl: string }> = ({ imageUrl }) => {
  return (
    <Grid
      size={{ xs: 12, md: 12, lg: 12 }}
      sx={{ height: "400px", overflow: "hidden", borderRadius: "16px" }}
    >
      <Box
        sx={{
          position: "relative",
          width: "100%",
          height: "100%",
        }}
      >
        <img
          src={imageUrl}
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
        ></Box>
      </Box>
    </Grid>
  );
};

export default LocationImage;
