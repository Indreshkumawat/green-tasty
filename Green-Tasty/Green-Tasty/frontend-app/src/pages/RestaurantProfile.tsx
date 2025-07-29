import {
  Breadcrumbs,
  Grid,
  Link,
  Typography,
  Stack,
  Alert,
} from "@mui/material";
import { useLocation } from "react-router-dom";
import LocationImage from "../components/locationImage";

import { useEffect, useState } from "react";
import { GetSpecialityDishes } from "../services/dishes";
import { type Dish } from "../interfaces/dish";
import DishCard from "../components/DishCard";
import Slider from "react-slick";
import DishCardSkeleton from "../skeletons/DishCardSkeleton";
import FeedbackTabs from "../components/FeedbackTabs";
import LocationBanner from "../components/LocationBanner";
function RestaurantProfile() {
  const { state } = useLocation();
  const location = state.location;
  const breadcrumbs = [
    <Link underline="hover" key="2" color="inherit" href="/home">
      Main Page
    </Link>,
    <Typography key="3" sx={{ color: "text.primary" }}>
      {location.address || "No address available"}
    </Typography>,
  ];
  const [specialDishes, setSpecialDishes] = useState<Dish[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  useEffect(() => {
    const fetchSpecialDishes = async () => {
      try {
        if (location) {
          const specialDishes = await GetSpecialityDishes(location.locationId);
          setSpecialDishes(specialDishes.data);
        }
      } catch (error) {
        console.error("Error fetching special dishes:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchSpecialDishes();
  }, []);
  const dishSliderSettings = {
    dots: false,
    infinite: true,
    speed: 500,
    slidesToShow: 4,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 2000,
    responsive: [
      {
        breakpoint: 960,
        settings: {
          slidesToShow: 2,
        },
      },
      {
        breakpoint: 600,
        settings: {
          slidesToShow: 1,
        },
      },
    ],
  };

  return (
    <Grid container spacing={2} sx={{ p: 2 }}>
      <Grid size={{ xs: 12 }}>
        <Breadcrumbs separator="›" aria-label="breadcrumb">
          {breadcrumbs}
        </Breadcrumbs>
      </Grid>
      <Grid
        size={{ xs: 4 }}
        sx={{
          display: "flex",
          flexDirection: "column",
          justifyContent: "space-between",
        }}
      >
        <LocationBanner location={location} />
      </Grid>

      <Grid size={{ xs: 8, md: 8, lg: 8 }}>
        <LocationImage imageUrl={location.imageUrl} />
      </Grid>
      <Grid size={{ xs: 12 }} sx={{ mt: 6 }}>
        <Typography variant="h6" sx={{ fontWeight: "bold" }}>
          Special Dishes
        </Typography>
        {isLoading ? (
          <DishCardSkeleton />
        ) : (specialDishes || []).length === 0 ? (
          <Stack sx={{ width: "100%", mt: 2 }}>
            <Alert variant="outlined" severity="info">
              No special dishes found
            </Alert>
          </Stack>
        ) : (
          <Slider {...dishSliderSettings}>
            {(specialDishes || []).map((dish: Dish, index: number) => (
              <div key={index}>
                <DishCard dish={dish} />
              </div>
            ))}
          </Slider>
        )}
      </Grid>
      <Grid size={{ xs: 12 }} sx={{ mt: 6 }}>
        <Typography variant="h6" sx={{ fontWeight: "bold" }}>
          Customer Reviews
        </Typography>

        <FeedbackTabs locationId={location.locationId} />
      </Grid>
    </Grid>
  );
}

export default RestaurantProfile;
