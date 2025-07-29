import { Box, Button, Select, Grid, MenuItem, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import { useEffect, useState } from "react";
import DishCard from "./DishCard";
import DishCardSkeleton from "../skeletons/DishCardSkeleton";
import { getReservationById } from "../services/reservations";

export default function ViewSelectedDishes({
  reservationId,
}: {
  reservationId: string;
}) {
  const { t } = useTranslation();
  const [selectedCategory, setSelectedCategory] = useState<string>("APPETIZER");
  const [selectedSort, setSelectedSort] = useState<string>("popularity,asc");
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [dishes, setDishes] = useState<any[]>([]);

  useEffect(() => {
    const fetchDishes = async () => {
      setIsLoading(true);
      const response = await getReservationById(
        reservationId,
        selectedCategory,
        selectedSort
      );
      console.log(response);
      setDishes(response.content);
      setIsLoading(false);
    };
    fetchDishes();
  }, [selectedSort, selectedCategory]);
  return (
    <Grid container spacing={2}>
      <Grid size={{ xs: 12 }}>
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between", // Distribute left/right
            alignItems: "center", // Align vertically center
            flexWrap: "wrap", // Optional: Wrap on small screens
            gap: 2,
          }}
        >
          <Box sx={{ display: "flex", gap: 2 }}>
            {["APPETIZER", "MAIN_COURSE", "DESSERT"].map((category) => (
              <Button
                key={category}
                variant={
                  selectedCategory === category ? "contained" : "outlined"
                }
                color="primary"
                onClick={() => setSelectedCategory(category)}
                sx={{
                  borderRadius: "8px",
                  textTransform: "capitalize",
                }}
              >
                {t(category.toLowerCase())}
              </Button>
            ))}
          </Box>

          <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
            <Typography>Sort By : </Typography>
            <Select
              value={selectedSort}
              onChange={(e) => setSelectedSort(e.target.value)}
              sx={{ minWidth: 200, borderRadius: "8px" }}
            >
              <MenuItem value="popularity,desc">Popularity Descending</MenuItem>
              <MenuItem value="popularity,asc">Popularity Ascending</MenuItem>
              <MenuItem value="price,desc">Price Descending</MenuItem>
              <MenuItem value="price,asc">Price Ascending</MenuItem>
            </Select>
          </Box>
        </Box>
      </Grid>
      <Grid size={{ xs: 12 }}>
        <Grid container spacing={2}>
          {isLoading ? (
            <DishCardSkeleton />
          ) : (
            dishes &&
            dishes.map((dish) => (
              <Grid
                size={{ xs: 12, sm: 6, md: 3 }}
                sx={{ mb: 4 }}
                key={dish.id}
              >
                <DishCard
                  dish={dish}
                  pageStatic={false}
                  reservationId={reservationId}
                />
              </Grid>
            ))
          )}
        </Grid>
      </Grid>
    </Grid>
  );
}
