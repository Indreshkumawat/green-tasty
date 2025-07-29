import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Typography from "@mui/material/Typography";
import CardActionArea from "@mui/material/CardActionArea";
import CardActions from "@mui/material/CardActions";
import Box from "@mui/material/Box";
import Skeleton from "@mui/material/Skeleton";
import { type Dish } from "../interfaces/dish";
import { useState } from "react";
import ViewDish from "./ViewDish";
import { Button } from "@mui/material";

export default function DishCard({
  dish,
  pageStatic = true,
  reservationId,
}: {
  dish: Dish;
  pageStatic?: boolean;
  reservationId?: string;
}) {
  const stateExists = dish.state?.toLowerCase() === "on stop";
  const [open, setOpen] = useState(false);
  const [imgLoaded, setImgLoaded] = useState(false);

  const handleOpen = () => {
    if (!stateExists) setOpen(true);
  };

  const handleClose = () => setOpen(false);

  return (
    <>
      <Card
        sx={{
          maxWidth: "70%",
          borderRadius: "16px",
          opacity: stateExists ? 0.5 : 1,
        }}
      >
        <CardActionArea onClick={handleOpen} disabled={stateExists}>
          <Box
            sx={{
              width: "200px",
              height: "200px",
              borderRadius: "50%",
              overflow: "hidden",
              mx: "auto",
              mt: 2,
              position: "relative",
            }}
          >
            {!imgLoaded && (
              <Skeleton
                variant="circular"
                width={200}
                height={200}
                sx={{ position: "absolute", top: 0, left: 0 }}
              />
            )}
            <img
              src={dish.imageUrl}
              alt={dish.name}
              style={{
                width: "100%",
                height: "100%",
                objectFit: "cover",
                display: imgLoaded ? "block" : "none",
              }}
              onLoad={() => setImgLoaded(true)}
            />
          </Box>
          <CardContent>
            <Typography
              gutterBottom
              variant="body1"
              sx={{ fontWeight: "bold" }}
            >
              {dish.name}
            </Typography>
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
          <Typography variant="body1" sx={{ color: "text.secondary" }}>
            {dish.price}
          </Typography>
          <Typography variant="body1" sx={{ color: "text.secondary" }}>
            {dish.weight}
          </Typography>
        </CardActions>
        {!pageStatic && (
          <Box sx={{ width: "100%" }}>
            <Button
              variant="contained"
              color="primary"
              fullWidth
              onClick={handleOpen}
            >
              Pre-order
            </Button>
          </Box>
        )}
      </Card>
      {open && (
        <ViewDish
          dish={dish}
          open={open}
          handleClose={handleClose}
          pageStatic={pageStatic}
          reservationId={reservationId}
        />
      )}
    </>
  );
}
