import React from "react";
import CustomAppBar from "../components/Appbar";
import { Grid } from "@mui/material";
import logoImg from "../assets/Logo.svg";
import { Outlet } from "react-router-dom";

const MainPage: React.FC = () => {
  console.log("MainPage rendered");
  return (
    <Grid
      container
      spacing={6}
      sx={{
        display: "flex",
        flexDirection: "column",
      }}
    >
      <Grid size={{ xs: 12, md: 12, lg: 12 }}>
        <CustomAppBar positioning="fixed" logo={logoImg} height={"5rem"} />
      </Grid>
      <Grid size={{ xs: 12, md: 12, lg: 12 }}>
        <Outlet />
      </Grid>
    </Grid>
  );
};

export default MainPage;
