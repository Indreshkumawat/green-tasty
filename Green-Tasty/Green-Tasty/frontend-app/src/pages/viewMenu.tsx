import { Breadcrumbs, Grid, Link, Typography } from "@mui/material";
import { useLocation } from "react-router-dom";
import ViewSelectedDishes from "../components/viewSelectedDishes";
import ViewAllDishes from "../components/viewAllDishes";
import ViewMenuImage from "../components/ViewMenuImage";
function ViewMenu() {
  const breadcrumbs = [
    <Link underline="hover" key="2" color="inherit" href="/home">
      Main Page
    </Link>,
    <Typography key="3" sx={{ color: "text.primary" }}>
      Menu
    </Typography>,
  ];
  const { state } = useLocation();
  const { reservationId } = state;
  return (
    <Grid container spacing={2} sx={{ p: 1 }}>
      <Grid size={{ xs: 12 }}>
        <Breadcrumbs separator="›" aria-label="breadcrumb">
          {breadcrumbs}
        </Breadcrumbs>
      </Grid>
      <Grid size={{ xs: 12 }} sx={{ pt: 4 }}>
        <ViewMenuImage />
      </Grid>
      <Grid size={{ xs: 12 }}>
        {state?.static ? (
          <ViewAllDishes />
        ) : (
          <ViewSelectedDishes reservationId={reservationId} />
        )}
      </Grid>
    </Grid>
  );
}

export default ViewMenu;
