import { IconButton, Button } from "@mui/material";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import Box from "@mui/material/Box";
import CloseIcon from "@mui/icons-material/Close";
import { type Dish } from "../interfaces/dish";
import { Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { GetDishById } from "../services/dishes";
import { addCartItem } from "../services/reservations";
import { enqueueSnackbar } from "notistack";

export default function ViewDish({
  dish,
  open,
  handleClose,
  pageStatic = true,
  reservationId,
}: {
  dish: Dish;
  open: boolean;
  handleClose: () => void;
  pageStatic: boolean;
  reservationId?: string;
}) {
  const [dishData, setDishData] = useState<Dish | null>(null);

  useEffect(() => {
    if (dish) {
      const fetchDish = async () => {
        const response = await GetDishById(dish.id);
        if (response.data) {
          setDishData(response.data);
        }
      };
      fetchDish();
    }
  }, [dish]); // ✅ include `dish` in the dependency array

  const handlePreOrder = async () => {
    try {
      if (!reservationId) {
        console.error("Reservation ID is required");
        return;
      }
      console.log("reservationId", reservationId, "dish.id", dish.id);

      const response = await addCartItem(reservationId, dish.id);
      console.log("response", response);
      if (response) {
        enqueueSnackbar(response.message, {
          variant: "success",
        });
        handleClose();
      } else {
        enqueueSnackbar(response?.data?.message || "Failed to add dish", {
          variant: "error",
        });
      }
    } catch (error: any) {
      console.error("Error adding cart item:", error);
      const message =
        error?.response?.data?.message || error?.message || "Unknown error";
      enqueueSnackbar(`Error adding cart item: ${message}`, {
        variant: "error",
      });
    }
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      PaperProps={{ sx: { borderRadius: "16px" } }}
    >
      <DialogTitle>
        <IconButton
          aria-label="close"
          onClick={handleClose}
          sx={{
            position: "absolute",
            right: 8,
            top: 8,
            color: (theme) => theme.palette.grey[500],
          }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent>
        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-start", // left-align the text
            gap: 1,
          }}
        >
          <Box
            sx={{
              alignSelf: "center",
              width: 200,
              height: 200,
              borderRadius: "50%",
              overflow: "hidden",
              mb: 2,
            }}
          >
            <img
              src={dish.imageUrl}
              alt={dish.name}
              style={{ width: "100%", height: "100%", objectFit: "cover" }}
            />
          </Box>

          <Typography variant="h6">{dishData?.name}</Typography>
          <Typography variant="body2">{dishData?.description}</Typography>
          <Typography variant="body2">
            <strong>Calories:</strong> {dishData?.calories}
          </Typography>
          <Typography variant="body2">
            <strong>Protein:</strong> {dishData?.proteins}
          </Typography>
          <Typography variant="body2">
            <strong>Fats:</strong> {dishData?.fats}
          </Typography>
          <Typography variant="body2">
            <strong>Carbohydrates:</strong> {dishData?.carbohydrates}
          </Typography>
          <Typography variant="body2">
            <strong>Vitamins:</strong> {dishData?.vitamins}
          </Typography>
          <Box
            sx={{
              display: "flex",
              justifyContent: "space-between",
              width: "100%",
            }}
          >
            <Typography variant="h5">{dish.price}</Typography>
            <Typography variant="h5">{dish.weight}</Typography>
          </Box>
          {!pageStatic && (
            <Button
              variant="contained"
              color="primary"
              fullWidth
              sx={{ mt: 2, borderRadius: "8px" }}
              onClick={handlePreOrder}
            >
              Pre-order
            </Button>
          )}
        </Box>
      </DialogContent>
    </Dialog>
  );
}
