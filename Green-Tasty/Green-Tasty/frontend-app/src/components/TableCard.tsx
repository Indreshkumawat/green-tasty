import { useState } from "react";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import CardMedia from "@mui/material/CardMedia";
import Typography from "@mui/material/Typography";
import { Box, Chip } from "@mui/material";
import { type Table } from "../interfaces/bookings";
import { type Location } from "../interfaces/locations";
import { useTranslation } from "react-i18next";
import { LocationPin } from "@mui/icons-material";
import dayjs from "dayjs";
import customParseFormat from "dayjs/plugin/customParseFormat";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import { SlotsDialog } from "./SlotDialog";
import AddIcon from "@mui/icons-material/Add";
import ActionReservation from "./actionReservation";
import EditCancelDialog from "./editCancelDialog";
import { type Reservation } from "../interfaces/reservations";
dayjs.extend(customParseFormat);

export default function TableCard({
  table,
  selectedLocation,
}: {
  table: Table;
  selectedLocation: Location;
}) {
  const { t } = useTranslation();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState<string | null>(null);
  const [selectedTime, setSelectedTime] = useState<string | null>(null);
  const [nextTime, setNextTime] = useState<string | null>(null);
  const [numberOfGuests, setNumberOfGuests] = useState<number>(1);
  const [showSuccessDialog, setShowSuccessDialog] = useState(false);
  const [reservationResponse, setReservationResponse] =
    useState<Reservation | null>(null);
  const [editMode, setEditMode] = useState(false);
  const handleShowAllClick = () => {
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
  };

  const handleSlotClick = (slot: string) => {
    setSelectedSlot(slot);
  };

  const handleCloseSlotDialog = () => {
    setSelectedSlot(null);
  };

  const slotsToDisplay = table.availableSlots.slice(0, 5);
  const showAllChip = table.availableSlots.length > 5;
  return (
    <>
      <Card sx={{ width: "100%", borderRadius: "16px" }}>
        <Box
          sx={{ display: "flex", flexDirection: "row", alignItems: "stretch" }}
        >
          <CardMedia
            component="img"
            image={selectedLocation.imageUrl || "/placeholder.jpg"}
            onError={(e) => (e.currentTarget.src = "/placeholder.jpg")}
            alt="Location"
            sx={{
              width: 200,
              height: 250,
              objectFit: "cover",
            }}
          />

          <Box sx={{ flex: 1, overflow: "hidden" }}>
            <CardContent>
              <Box
                sx={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  gap: 2,
                }}
              >
                <Box
                  sx={{
                    display: "flex",
                    alignItems: "center",
                    gap: 1,
                    flexShrink: 1,
                    overflow: "hidden",
                  }}
                >
                  <LocationPin
                    fontSize="small"
                    sx={{ color: "text.secondary" }}
                  />
                  <Typography variant="body1" noWrap sx={{ fontWeight: 500 }}>
                    {selectedLocation.address}
                  </Typography>
                </Box>

                <Typography
                  variant="body1"
                  color="text.secondary"
                  sx={{ whiteSpace: "nowrap" }}
                >
                  {t("table")} {table.tableNumber}
                </Typography>
              </Box>

              <Typography variant="body1" sx={{ mt: 1, fontWeight: 500 }}>
                {t("table_seating_capacity")}: {table.capacity} {t("people")}
              </Typography>
              <Typography variant="body1" sx={{ mt: 1, fontWeight: 500 }}>
                {table.availableSlots.length} {t("slots_available_for")}{" "}
                {dayjs(table.date, "DD-MM-YYYY").format("MMM DD, YYYY")}
              </Typography>

              <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1, mt: 1 }}>
                {slotsToDisplay.map((slot) => (
                  <Chip
                    key={slot}
                    label={slot}
                    icon={<AccessTimeIcon />}
                    onClick={() => handleSlotClick(slot)}
                    sx={{
                      fontWeight: 500,
                      width: 200,
                      backgroundColor: "transparent",
                      border: "1px solid",
                      borderColor: "primary.main",
                      borderRadius: 2,
                    }}
                  />
                ))}
                {showAllChip && (
                  <Chip
                    label={
                      <Box
                        sx={{
                          display: "flex",
                          alignItems: "flex-start",
                          gap: 1,
                        }}
                      >
                        <AddIcon color="primary" />
                        {t("show_all")}
                      </Box>
                    }
                    sx={{
                      fontWeight: 500,
                      width: 200,
                      backgroundColor: "transparent",
                      border: "1px solid",
                      borderColor: "primary.main",
                      borderRadius: 2,
                      cursor: "pointer",
                    }}
                    onClick={handleShowAllClick}
                  />
                )}
              </Box>
            </CardContent>
          </Box>
        </Box>

        <SlotsDialog
          table={table}
          selectedLocation={selectedLocation}
          open={dialogOpen}
          setSelectedSlot={setSelectedSlot}
          onClose={handleCloseDialog}
        />
      </Card>

      {/* Slot detail dialog */}
      {selectedSlot && (
        <ActionReservation
          selectedSlot={selectedSlot}
          reservationResponse={reservationResponse || null}
          setShowSuccessDialog={setShowSuccessDialog}
          handleCloseSlotDialog={handleCloseSlotDialog}
          table={table}
          selectedLocation={selectedLocation}
          setSelectedTime={setSelectedTime}
          setNextTime={setNextTime}
          setNumberOfGuests={setNumberOfGuests}
          selectedTime={selectedTime || ""}
          nextTime={nextTime || ""}
          numberOfGuests={numberOfGuests}
          setReservationResponse={setReservationResponse}
          editMode={editMode}
          setEditMode={setEditMode}
        />
      )}
      {reservationResponse && (
        <EditCancelDialog
          showSuccessDialog={showSuccessDialog}
          setShowSuccessDialog={setShowSuccessDialog}
          handleCloseSlotDialog={handleCloseSlotDialog}
          table={table}
          setSelectedSlot={setSelectedSlot}
          selectedTime={selectedTime || ""}
          nextTime={nextTime || ""}
          selectedLocation={selectedLocation}
          selectedGuest={numberOfGuests.toString()}
          reservationResponse={reservationResponse}
          setEditMode={setEditMode}
        />
      )}
    </>
  );
}
