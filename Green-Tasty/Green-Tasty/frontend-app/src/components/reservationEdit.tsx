import {
  IconButton,
  Typography,
  DialogContent,
  Box,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  Button,
} from "@mui/material";
import { Close as CloseIcon } from "@mui/icons-material";
import { useTranslation } from "react-i18next";
import dayjs from "dayjs";
import customParseFormat from "dayjs/plugin/customParseFormat";
import GroupIcon from "@mui/icons-material/Group";
import RemoveIcon from "@mui/icons-material/Remove";
import AddBoxIcon from "@mui/icons-material/AddBox";
import { useEffect, useState } from "react";
import { enqueueSnackbar } from "notistack";
import { type Reservation } from "../interfaces/reservations";
import { editReservation } from "../services/reservations";

dayjs.extend(customParseFormat);

export default function EditReservation({
  reservation,
  setRefresh,
  onClose,
}: {
  reservation: Reservation;
  onClose: () => void;
  setRefresh: (refresh: boolean) => void;
}) {
  const { t } = useTranslation();
  const timeSlots = [
    "10:30",
    "12:00",
    "12:15",
    "13:45",
    "14:00",
    "15:30",
    "15:45",
    "17:15",
    "17:30",
    "19:00",
    "19:15",
    "20:45",
    "21:00",
    "22:30",
    "22:45",
  ];

  const [selectedTime, setSelectedTime] = useState("");
  const [nextTime, setNextTime] = useState("");
  const [numberOfGuests, setNumberOfGuests] = useState(
    Number(reservation.guestsNumber) ?? 1
  );

  useEffect(() => {
    if (reservation.timeSlot?.includes("-")) {
      const [start, end] = reservation.timeSlot.split("-");
      setSelectedTime(start.trim());
      setNextTime(end.trim());
    }
  }, [reservation.timeSlot]);

  const handleTimeChange = (value: string) => {
    setSelectedTime(value);
    const calculated = dayjs(value, "HH:mm").add(90, "minute").format("HH:mm");
    if (timeSlots.includes(calculated)) {
      setNextTime(calculated);
    } else {
      setNextTime("");
    }
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const reservationData = {
      guestsNumber: numberOfGuests.toString(),
      timeFrom: selectedTime,
      timeTo: nextTime,
    };

    try {
      const response = await editReservation(reservation.id, reservationData);
      if (response?.error) {
        enqueueSnackbar(response.error, { variant: "error" });
      } else {
        enqueueSnackbar(t("reservation_successful"), { variant: "success" });
        onClose?.();
        setRefresh(true);
      }
    } catch (error) {
      enqueueSnackbar("Error editing reservation", { variant: "error" });
      console.error("Error editing reservation:", error);
    }
  };
  return (
    <Dialog open={Boolean(reservation)} onClose={onClose}>
      <DialogTitle
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        {t("edit_reservation")}
        <IconButton onClick={onClose}>
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent>
        <form onSubmit={handleSubmit}>
          <Typography variant="body2">
            {t("you_are_editing_a_reservation_at")}{" "}
            {reservation.locationAddress},{" "}
            <strong>
              {dayjs(reservation.date, "DD-MM-YYYY").format("MMM DD, YYYY")}
            </strong>
          </Typography>

          {/* Guest Count Section */}
          <Typography variant="h6" sx={{ mt: 2 }}>
            {t("guests")}
          </Typography>
          <Typography variant="body2" sx={{ mt: 1 }}>
            {t("please_specify_the_number_of_guests")}
          </Typography>

          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              gap: 2,
              mt: 2,
              border: "1px solid",
              borderColor: "primary.main",
              borderRadius: 2,
              pl: 1,
            }}
          >
            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
              <GroupIcon fontSize="medium" />
              <Typography variant="body1">{t("guests")}</Typography>
            </Box>

            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
              <IconButton
                onClick={() => setNumberOfGuests(Number(numberOfGuests) - 1)}
                disabled={Number(numberOfGuests) <= 1}
              >
                <RemoveIcon fontSize="large" />
              </IconButton>
              <Typography variant="h6">{numberOfGuests}</Typography>
              <IconButton
                onClick={() => setNumberOfGuests(numberOfGuests + 1)}
                disabled={Number(numberOfGuests) >= 4}
              >
                <AddBoxIcon fontSize="large" />
              </IconButton>
            </Box>
          </Box>

          {/* Time Selection */}
          <Typography variant="h6" sx={{ mt: 2 }}>
            {t("time")}
          </Typography>
          <Typography variant="body2" sx={{ mt: 1 }}>
            {t("please_choose_your_preferred_time_from_the_dropdowns_below")}
          </Typography>

          <Box sx={{ display: "flex", gap: 2, mt: 2 }}>
            <Select
              value={selectedTime}
              onChange={(e) => handleTimeChange(e.target.value)}
              displayEmpty
              sx={{
                minWidth: "45%",
                border: "1px solid",
                borderColor: "primary.main",
                borderRadius: "8px",
              }}
            >
              <MenuItem value="" disabled>
                Select Time
              </MenuItem>
              {timeSlots.map((slot) => (
                <MenuItem key={slot} value={slot}>
                  {slot}
                </MenuItem>
              ))}
            </Select>

            <Select
              value={nextTime}
              disabled
              sx={{
                minWidth: "45%",
                border: "1px solid",
                borderRadius: "8px",
                borderColor: "primary.main",
              }}
            >
              <MenuItem value={nextTime}>{nextTime || "Next time"}</MenuItem>
            </Select>
          </Box>

          <Button
            type="submit"
            variant="contained"
            color="primary"
            sx={{ mt: 2, borderRadius: "8px", p: 2 }}
            fullWidth
          >
            {t("edit_reservation")}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}
