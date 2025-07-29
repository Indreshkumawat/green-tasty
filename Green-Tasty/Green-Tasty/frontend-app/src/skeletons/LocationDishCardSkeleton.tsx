import {
  Card,
  CardActionArea,
  CardContent,
  CardActions,
  Skeleton,
  Box,
} from "@mui/material";
import Slider from "react-slick";

export default function LocationDishCardSkeleton() {
  const locationSliderSettings = {
    dots: false,
    infinite: true,
    speed: 500,
    slidesToShow: 3,
    slidesToScroll: 1,
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
    <Slider {...locationSliderSettings}>
      {Array.from({ length: 5 }).map((_, index) => (
        <Box key={index} px={1}>
          <Card sx={{ maxWidth: "100%", height: "80%", borderRadius: "16px" }}>
            <CardActionArea>
              <Skeleton variant="rectangular" height={140} width="100%" />
              <CardContent>
                <Box display="flex" alignItems="center" mb={1}>
                  <Skeleton variant="circular" width={24} height={24} />
                  <Skeleton variant="text" width="60%" sx={{ ml: 1 }} />
                </Box>
                <Skeleton variant="text" width="100%" />
                <Skeleton variant="text" width="90%" />
              </CardContent>
            </CardActionArea>
            <CardActions
              sx={{
                display: "flex",
                justifyContent: "space-between",
                pr: 3,
                pl: 2,
                pb: 2,
              }}
            >
              <Skeleton variant="text" width="45%" />
              <Skeleton variant="text" width="40%" />
            </CardActions>
          </Card>
        </Box>
      ))}
    </Slider>
  );
}
