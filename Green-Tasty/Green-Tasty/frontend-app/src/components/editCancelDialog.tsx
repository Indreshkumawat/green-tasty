import {
  Dialog,
  Typography,
  DialogContent,
  DialogTitle,
  Button,
  Box,
  IconButton,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";
import { type Table } from "../interfaces/bookings";
import { type Location } from "../interfaces/locations";
import { type Reservation } from "../interfaces/reservations";
import { cancelReservation } from "../services/reservations";
import { enqueueSnackbar } from "notistack";
function EditCancelDialog({
  showSuccessDialog,
  setShowSuccessDialog,
  handleCloseSlotDialog,
  table,
  selectedTime,
  nextTime,
  selectedLocation,
  selectedGuest,
  reservationResponse,
  setSelectedSlot,
  setEditMode,
}: {
  showSuccessDialog: boolean;
  setShowSuccessDialog: (showSuccessDialog: boolean) => void;
  handleCloseSlotDialog: () => void;
  table: Table;
  selectedTime: string;
  nextTime: string;
  selectedLocation: Location;
  selectedGuest: string;
  reservationResponse: Reservation;
  setSelectedSlot: (selectedSlot: string) => void;
  setEditMode: (editMode: boolean) => void;
}) {
  const { t } = useTranslation();
  const handleClose = () => {
    setShowSuccessDialog(false);
    handleCloseSlotDialog();
  };
  const handleCancelReservation = async () => {
    if (reservationResponse.id) {
      try {
        console.log("reservationResponse.id ", reservationResponse.id);
        const response = await cancelReservation(reservationResponse.id);
        if (response.error) {
          enqueueSnackbar(response.error, {
            variant: "error",
          });
        } else {
          enqueueSnackbar(t("reservation_cancelled"), {
            variant: "success",
          });
          handleClose();
        }
      } catch (error) {
        console.error("Error canceling reservation:", error);
      }
    }
  };

  const handleEditReservation = () => {
    setSelectedSlot(reservationResponse.timeSlot);
    setEditMode(true);
    setShowSuccessDialog(false);
  };

  return (
    <Dialog
      open={showSuccessDialog}
      onClose={handleClose}
      PaperProps={{
        sx: { borderRadius: "8px" }, // ✅ 8px border radius
      }}
    >
      <DialogTitle
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        {t("reservation_confirmed")}!
        <IconButton onClick={handleClose}>
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent>
        <Typography>
          Your table reservation at Green & Tasty for{" "}
          <strong>{selectedGuest}</strong> people on{" "}
          {dayjs(table.date, "DD-MM-YYYY").format("MMM DD, YYYY")}, from{" "}
          <strong>{selectedTime}</strong> to <strong>{nextTime}</strong> at
          Table <strong>{table.tableNumber}</strong> has been successfully made.
        </Typography>
        <Typography sx={{ mt: 2 }}>
          We look forward to welcoming you at {selectedLocation.address}
        </Typography>
        <Typography sx={{ mt: 2 }}>
          If you need to modify or cancel your reservation, you can do so up to
          30 min. before the reservation time.
        </Typography>
        <Box sx={{ display: "flex", justifyContent: "space-between", gap: 2 }}>
          <Button
            variant="contained"
            onClick={handleCancelReservation}
            sx={{
              mt: 3,
              backgroundColor: "white",
              color: "primary.main",
              border: "2px solid",
              borderColor: "primary.main",
              borderRadius: "8px",
            }}
            fullWidth
          >
            {t("cancel_reservation")}
          </Button>
          <Button
            variant="contained"
            sx={{ mt: 3, color: "white", borderRadius: "8px" }}
            onClick={handleEditReservation}
            fullWidth
          >
            {t("edit_reservation")}
          </Button>
        </Box>
      </DialogContent>
    </Dialog>
  );
}

export default EditCancelDialog;
