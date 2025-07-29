import {
  Card,
  CardActionArea,
  CardActions,
  Skeleton,
  Box,
  CardContent,
} from "@mui/material";

import Slider from "react-slick";

export default function DishCardSkeleton() {
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
    <Slider {...dishSliderSettings}>
      {Array.from({ length: 5 }).map((_, index) => (
        <div key={index}>
          <Card sx={{ maxWidth: "70%", borderRadius: "16px", mx: "auto" }}>
            <CardActionArea>
              <Box
                sx={{
                  width: "200px",
                  height: "200px",
                  borderRadius: "50%",
                  overflow: "hidden",
                  mx: "auto",
                  mt: 2,
                }}
              >
                <Skeleton variant="circular" width={200} height={200} />
              </Box>
              <CardContent>
                <Skeleton variant="text" width="60%" sx={{ mx: "auto" }} />
              </CardContent>
            </CardActionArea>
            <CardActions
              sx={{
                display: "flex",
                justifyContent: "space-between",
                pr: 3,
                pl: 3,
                pb: 3,
              }}
            >
              <Skeleton variant="text" width="30%" />
              <Skeleton variant="text" width="30%" />
            </CardActions>
          </Card>
        </div>
      ))}
    </Slider>
  );
}
