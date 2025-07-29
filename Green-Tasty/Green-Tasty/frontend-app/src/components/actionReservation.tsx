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
import { type Table } from "../interfaces/bookings";
import { type Location } from "../interfaces/locations";
import GroupIcon from "@mui/icons-material/Group";
import RemoveIcon from "@mui/icons-material/Remove";
import AddBoxIcon from "@mui/icons-material/AddBox";
import { useEffect } from "react";
import { makeReservation } from "../services/bookings";
import { enqueueSnackbar } from "notistack";
import { type Reservation } from "../interfaces/reservations";
import { editReservation } from "../services/reservations";
dayjs.extend(customParseFormat);

export default function ActionReservation({
  selectedSlot,
  handleCloseSlotDialog,
  table,
  selectedLocation,
  selectedTime,
  nextTime,
  numberOfGuests,
  reservationResponse,
  setSelectedTime,
  setNextTime,
  setNumberOfGuests,
  setShowSuccessDialog,
  setReservationResponse,
  setEditMode,
  editMode,
}: {
  selectedSlot: string;
  handleCloseSlotDialog: () => void;
  table: Table;
  selectedLocation: Location;
  selectedTime: string;
  nextTime: string;
  numberOfGuests: number;
  reservationResponse: Reservation | null;
  setSelectedTime: (time: string) => void;
  setNextTime: (time: string) => void;
  setNumberOfGuests: (guests: number) => void;
  setShowSuccessDialog: (showSuccessDialog: boolean) => void;
  setReservationResponse: (reservationResponse: Reservation) => void;
  setEditMode: (editMode: boolean) => void;
  editMode: boolean;
}) {
  const tableSlot1 = [
    "10:30",
    "12:15",
    "14:00",
    "15:45",
    "17:30",
    "19:15",
    "21:00",
  ];
  const tableSlot2 = [
    "12:00",
    "13:45",
    "15:30",
    "17:15",
    "19:00",
    "20:45",
    "22:30",
  ];

  const { t } = useTranslation();

  useEffect(() => {
    if (selectedSlot.includes("-")) {
      const [start, end] = selectedSlot.split("-");
      setSelectedTime(start.trim());
      setNextTime(end.trim());
    }
  }, [selectedSlot]);

  const handleTimeChange = (value: string) => {
    setSelectedTime(value);
    const calculated = dayjs(value, "HH:mm").add(90, "minute").format("HH:mm");
    // Find the next available time in tableSlot2 that is closest to calculated time
    const nextAvailableTime = tableSlot2.find((slot) => {
      const slotTime = dayjs(slot, "HH:mm");
      const calculatedTime = dayjs(calculated, "HH:mm");
      return (
        slotTime.isAfter(calculatedTime) || slotTime.isSame(calculatedTime)
      );
    });
    setNextTime(nextAvailableTime || "");
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    // locationId: string;
    // tableNumber: string[];
    // date: string;
    // guestsNumber: string;
    // timeFrom: string;
    // timeTo: string;
    // You can handle form submission here
    const reservationData = {
      locationId: selectedLocation.locationId,
      tableNumber: [table.tableNumber],
      date: dayjs(table.date, "DD-MM-YYYY").format("YYYY-MM-DD"),
      guestsNumber: numberOfGuests.toString(),
      timeFrom: selectedTime,
      timeTo: nextTime,
    };
    try {
      let response;

      if (editMode) {
        console.log("editMode ", editMode);
        response = await editReservation(
          reservationResponse?.id || "",
          reservationData
        );
      } else {
        response = await makeReservation(reservationData);
      }
      if (response?.error) {
        enqueueSnackbar(response.error, {
          variant: "error",
        });
      } else {
        enqueueSnackbar(t("reservation_successful"), {
          variant: "success",
        });
        console.log("response.data ", response?.data);
        setReservationResponse(response?.data || null);
        handleCloseSlotDialog();
        setShowSuccessDialog(true);
        setEditMode(false);
      }
    } catch (error) {
      console.error("Error making reservation:", error);
    }
    // Possibly call an API or pass values to parent
  };

  return (
    <>
      <Dialog open={!!selectedSlot} onClose={handleCloseSlotDialog}>
        <DialogTitle
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          {editMode ? t("edit_reservation") : t("make_a_reservation")}
          <IconButton
            onClick={() => {
              handleCloseSlotDialog();
              setEditMode(false);
            }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>

        <DialogContent>
          <form onSubmit={handleSubmit}>
            <Typography variant="body2">
              {editMode
                ? t("you_are_editing_a_reservation_at")
                : t("you_are_making_a_reservation_at")}{" "}
              {selectedLocation.address}, {t("table")} {table.tableNumber},{" "}
              {t("for")}{" "}
              <strong>
                {dayjs(table.date, "DD-MM-YYYY").format("MMM DD, YYYY")}
              </strong>
            </Typography>

            {/* Guest Count Section */}
            <Typography variant="h6" sx={{ mt: 2 }}>
              {t("guests")}
            </Typography>
            <Typography variant="body2" sx={{ mt: 1 }}>
              {t("please_specify_the_number_of_guests")}
            </Typography>
            <Typography variant="body2" sx={{ mt: 1 }}>
              {t("table_seating_capacity")} :{" "}
              <strong>
                {table.capacity} {t("people")}
              </strong>
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
                  onClick={() => setNumberOfGuests(numberOfGuests - 1)}
                  disabled={numberOfGuests === 1}
                >
                  <RemoveIcon fontSize="large" />
                </IconButton>
                <Typography variant="h6">{numberOfGuests}</Typography>
                <IconButton
                  onClick={() => setNumberOfGuests(numberOfGuests + 1)}
                  disabled={numberOfGuests === Number(table.capacity)}
                >
                  <AddBoxIcon fontSize="large" />
                </IconButton>
              </Box>
            </Box>

            {/* Time Slots Section */}
            <Typography variant="h6" sx={{ mt: 2 }}>
              {t("time")}
            </Typography>
            <Typography variant="body2" sx={{ mt: 1 }}>
              {t("please_choose_your_preferred_time_from_the_dropdowns_below")}
            </Typography>

            <Box
              sx={{
                display: "flex",
                gap: 2,
                mt: 2,
                width: "100%",
                justifyContent: "space-between",
              }}
            >
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
                {tableSlot1.map((slot) => (
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
                {nextTime ? (
                  <MenuItem value={nextTime}>{nextTime}</MenuItem>
                ) : (
                  <MenuItem value="">Next time</MenuItem>
                )}
              </Select>
            </Box>

            <Button
              type="submit"
              variant="contained"
              color="primary"
              sx={{ mt: 2, borderRadius: "8px", p: 2 }}
              fullWidth
            >
              {editMode ? t("edit_reservation") : t("make_a_reservation")}
            </Button>
          </form>
        </DialogContent>
      </Dialog>
    </>
  );
}
