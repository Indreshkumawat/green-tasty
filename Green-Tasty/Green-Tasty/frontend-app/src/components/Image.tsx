import { Box, Grid } from "@mui/material";
import SignUpImage from "../assets/SignUpImage.svg";
import React from "react";
const Image: React.FC<{
  width?: string;
  height?: string;
}> = ({ width = "75%", height = "95%" }) => {
  return (
    <Grid
      size={{ xs: 12, md: 6, lg: 6 }}
      sx={{
        display: { xs: "none", md: "flex" },
        height: "100%",
        justifyContent: "center",
        alignItems: "center",
        p: 2,
      }}
    >
      <Box
        sx={{
          position: "relative",
          width: width,
          height: height,
          borderRadius: "2rem",
          mb: 2,
          boxShadow: 5,
          overflow: "hidden",
        }}
      >
        <Box
          sx={{
            backgroundImage: `url(${SignUpImage})`,
            backgroundSize: "cover",
            backgroundPosition: "center",
            width: "100%",
            height: "100%",
          }}
        ></Box>
      </Box>
    </Grid>
  );
};

export default Image;
