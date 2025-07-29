import { useEffect, useState } from "react";
import { Grid, Typography } from "@mui/material";
import ViewBannerImage from "../components/ViewBannerImage";
import DishCard from "../components/DishCard";
import { useTranslation } from "react-i18next";
import { GetPopularDishes } from "../services/dishes";
import { type Dish } from "../interfaces/dish";
import Alert from "@mui/material/Alert";
import Stack from "@mui/material/Stack";

import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import { GetAllLocations } from "../services/locations";
import { type Location } from "../interfaces/locations";
import LocationDishCard from "../components/locationDishCard";
import LocationDishCardSkeleton from "../skeletons/LocationDishCardSkeleton";
import DishCardSkeleton from "../skeletons/DishCardSkeleton";

export default function HomePage() {
  const { t } = useTranslation();
  const [dishes, setDishes] = useState<Dish[]>([]);
  const [locations, setLocations] = useState<Location[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const [getPopularDishes, getLocations] = await Promise.all([
          GetPopularDishes(),
          GetAllLocations(),
        ]);

        if (getPopularDishes) setDishes(getPopularDishes);
        if (getLocations) setLocations(getLocations.data);
      } catch (err: any) {
        const errorMessage =
          err?.response?.data?.message ||
          err?.message ||
          "An unknown error occurred.";
        setError(errorMessage);
        console.error("Error fetching data:", err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
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

  const locationSliderSettings = {
    dots: false,
    infinite: true,
    slidesToShow: 3,
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
    <Grid container spacing={6}>
      <Grid size={12}>
        <ViewBannerImage />
      </Grid>

      {error && (
        <Grid size={12}>
          <Stack sx={{ width: "100%", px: 2 }}>
            <Alert variant="outlined" severity="error">
              {error}
            </Alert>
          </Stack>
        </Grid>
      )}

      {!error && (
        <Grid size={12} p={2}>
          <Typography variant="h5" sx={{ textAlign: "start", mt: 4, mb: 6 }}>
            {t("most_popular_dishes")}
          </Typography>

          {isLoading ? (
            <DishCardSkeleton />
          ) : dishes.length > 0 ? (
            <Slider {...dishSliderSettings}>
              {dishes.map((dish, index) => (
                <div key={index}>
                  <DishCard dish={dish} />
                </div>
              ))}
            </Slider>
          ) : (
            <Typography>{t("no_dishes_available")}</Typography>
          )}

          <Typography variant="h5" sx={{ textAlign: "start", mt: 10, mb: 6 }}>
            {t("locations")}
          </Typography>

          {isLoading ? (
            <LocationDishCardSkeleton />
          ) : locations.length > 0 ? (
            <Slider {...locationSliderSettings}>
              {locations.map((location, index) => (
                <div key={index}>
                  <LocationDishCard {...location} />
                </div>
              ))}
            </Slider>
          ) : (
            <Typography>{t("no_locations_available")}</Typography>
          )}
        </Grid>
      )}
    </Grid>
  );
}
