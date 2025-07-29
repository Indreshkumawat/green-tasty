import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
} from "@mui/material";
import { type Reservation } from "../interfaces/reservations";
import { cancelReservation } from "../services/reservations";
import { enqueueSnackbar } from "notistack";
import { useTranslation } from "react-i18next";
export default function CancelDialog({
  reservation,
  onClose,
  setRefresh,
}: {
  reservation: Reservation;
  onClose: () => void;
  setRefresh: (refresh: boolean) => void;
}) {
  const { t } = useTranslation();
  const handleCancel = async () => {
    try {
      await cancelReservation(reservation.id);
      enqueueSnackbar(t("reservation_canceled_successfully"), {
        variant: "success",
      });
      onClose();
      setRefresh(true);
    } catch (error) {
      enqueueSnackbar(
        (error as any)?.response?.data?.message ||
          t("error_canceling_reservation"),
        {
          variant: "error",
        }
      );
      console.error("Error canceling reservation:", error);
    }
  };
  return (
    <Dialog
      open={Boolean(reservation)}
      onClose={onClose}
      sx={{ borderRadius: 4, p: 2 }}
    >
      <DialogTitle>{t("cancel_reservation")}</DialogTitle>
      <DialogContent>
        <Typography>
          {t("are_you_sure_you_want_to_cancel_this_reservation")}
        </Typography>
      </DialogContent>
      <DialogActions sx={{ p: 2 }}>
        <Button variant="outlined" onClick={handleCancel}>
          {t("cancel")}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
