import {
  Button,
  Card,
  CardActions,
  Typography,
  CardContent,
  Box,
  Chip,
  Modal,
} from "@mui/material";
import { type Reservation } from "../interfaces/reservations";
import { useTranslation } from "react-i18next";
import {
  Group,
  AccessTime,
  CalendarMonth,
  LocationOn,
  RestaurantMenu,
  Person,
} from "@mui/icons-material";
import EditReservation from "./reservationEdit";
import CancelDialog from "./cancelDialog";
import { useState } from "react";
import FeedbackDialog from "./feedbackDialog";
import { useNavigate } from "react-router-dom";
//import Cookies from "js-cookie";
import WaiterActionReservation from "./waiterActionReservation";
import dayjs from "dayjs";

function ReservationCard({
  reservation,
  setRefresh,
}: {
  reservation: Reservation;
  setRefresh: (refresh: boolean) => void;
}) {
  const { t } = useTranslation();
  const [isEdited, setEditMode] = useState(false);
  const [isCanceled, setCanceled] = useState(false);
  const [isFeedback, setFeedback] = useState(false);
  const [isUpdateFeedback, setUpdateFeedback] = useState(false);
  const [isWaiterDialogOpen, setWaiterDialogOpen] = useState(false);
  const role = localStorage.getItem("role") || "";

  const status = reservation.status?.toLowerCase() || "reserved";
  const navigate = useNavigate();

  // Custom color mapping for chip
  const chipStyleMap: Record<string, { label: string; color: string }> = {
    cancelled: { label: t("cancelled"), color: "#FCE9ED" },
    finished: { label: t("finished"), color: "#E9FFEA" },
    reserved: { label: t("reserved"), color: "#FFF2D4" },
    pending_review: { label: t("pending_for_review"), color: "#DEE9FF" },
  };

  const chipData = chipStyleMap[status] || {
    label: status,
    color: "#e0e0e0",
  };

  const handlePreOrder = () => {
    navigate("/view-menu", {
      state: { reservationId: reservation.id, static: false },
    });
  };

  const handleCancel = () => {
    setCanceled(true);
  };

  const handleEdit = () => {
    if (role === "waiter") {
      setWaiterDialogOpen(true);
    } else {
      setEditMode(true);
    }
  };

  const handleLeaveFeedback = () => {
    setFeedback(true);
    setUpdateFeedback(false);
  };

  const handleUpdateFeedback = () => {
    setFeedback(false);
    setUpdateFeedback(true);
  };

  const handleWaiterDialogClose = () => {
    setWaiterDialogOpen(false);
  };

  const handleWaiterSuccess = () => {
    setWaiterDialogOpen(false);
    // Add any success logic here (e.g., refresh data)
  };

  return (
    <>
      <Card
        sx={{
          width: "85%",
          display: "flex",
          flexDirection: "column",
          justifyContent: "space-between",
          height: 330,
          maxHeight: 350,
          borderRadius: 4,
          position: "relative",
          overflow: "visible",
          p: 2,
        }}
      >
        <Box
          sx={{
            position: "relative",
            display: "flex",
            flexDirection: "column",
            gap: 1,
          }}
        >
          <CardContent
            sx={{ display: "flex", flexDirection: "column", gap: 2, p: 1 }}
          >
            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
              <LocationOn fontSize="medium" />
              <Typography variant="body1" sx={{ color: "text.secondary" }}>
                {reservation.locationAddress}
              </Typography>
            </Box>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
              <CalendarMonth fontSize="medium" />
              <Typography variant="body1" sx={{ color: "text.secondary" }}>
                {reservation.date}
              </Typography>
            </Box>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
              <AccessTime fontSize="medium" />
              <Typography variant="body1" sx={{ color: "text.secondary" }}>
                {reservation.timeSlot}
              </Typography>
            </Box>
            {role === "waiter" && (
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <Person fontSize="medium" />
                <Typography variant="body1" sx={{ color: "text.secondary" }}>
                  {reservation.customerName && reservation.customerName.trim() !== ""
                    ? reservation.customerName
                    : `Visitor ${reservation.visitorId}`}
                </Typography>
              </Box>
            )}

            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
              <RestaurantMenu fontSize="medium" />
              <Typography variant="body1" sx={{ color: "text.secondary" }}>
                {Object.values(reservation.preOrder).length} {t("dishes")}
              </Typography>
            </Box>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
              <Group fontSize="medium" />
              <Typography variant="body2" sx={{ color: "text.secondary" }}>
                {reservation.guestsNumber} {t("guests")}
              </Typography>
            </Box>
          </CardContent>

          {/* Status Chip */}
          <Chip
            label={t(chipData.label)}
            size="small"
            sx={{
              position: "absolute",
              top: 8,
              right: 8,
              backgroundColor: chipData.color,
              color: "black",
              borderRadius: "999px",
            }}
          />
        </Box>
        {status !== "cancelled" && (
          <CardActions>
            {status !== "pending_review" && status !== "finished" && (
              <>
                <Box
                  sx={{ ml: "auto", display: "flex", gap: 1, width: "100%" }}
                >
                  <Button
                    variant="outlined"
                    color="error"
                    sx={{ borderRadius: 2, width: "100%" }}
                    onClick={handleCancel}
                  >
                    {t("cancel")}
                  </Button>
                  <Button
                    variant="outlined"
                    color="primary"
                    sx={{ borderRadius: 2, width: "100%" }}
                    onClick={handleEdit}
                  >
                    {role === "client" ? t("edit") : t("postpone")}
                  </Button>
                  {role === "client" && (
                    <Button
                      variant="contained"
                      color="primary"
                      fullWidth
                      sx={{ borderRadius: 2 }}
                      onClick={handlePreOrder}
                    >
                      {t("pre-order")}
                    </Button>
                  )}
                </Box>
              </>
            )}
            {status === "pending_review" && (
              <Box sx={{ ml: "auto", display: "flex", gap: 1 }}>
                <Button
                  variant="outlined"
                  color="primary"
                  size="medium"
                  sx={{ borderRadius: 2, width: "100%" }}
                  onClick={handleLeaveFeedback}
                >
                  {t("leave_feedback")}
                </Button>
              </Box>
            )}
            {status === "finished" && (
              <Box sx={{ ml: "auto", display: "flex", gap: 1 }}>
                <Button
                  variant="outlined"
                  color="primary"
                  size="medium"
                  sx={{ borderRadius: 2, width: "100%" }}
                  onClick={handleUpdateFeedback}
                >
                  {t("update_feedback")}
                </Button>
              </Box>
            )}
          </CardActions>
        )}
      </Card>

      {/* Dialogs */}
      {isEdited && (
        <EditReservation
          reservation={reservation}
          setRefresh={setRefresh}
          onClose={() => setEditMode(false)}
        />
      )}

      {isCanceled && (
        <CancelDialog
          reservation={reservation}
          setRefresh={setRefresh}
          onClose={() => setCanceled(false)}
        />
      )}

      {isFeedback && (
        <FeedbackDialog
          reservation={reservation}
          isUpdateFeedback={false}
          onClose={() => setFeedback(false)}
          setRefresh={setRefresh}
          waiterId={reservation.waiterEmail || ""}
          feedbackId={reservation.feedbackId || ""}
        />
      )}

      {isUpdateFeedback && (
        <FeedbackDialog
          reservation={reservation}
          isUpdateFeedback={true}
          setRefresh={setRefresh}
          onClose={() => setUpdateFeedback(false)}
          waiterId={reservation.waiterEmail || ""}
          feedbackId={reservation.feedbackId || ""}
        />
      )}

      {/* Waiter Action Dialog */}
      <Modal
        open={isWaiterDialogOpen}
        onClose={handleWaiterDialogClose}
        sx={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <Box sx={{ outline: "none" }}>
          <WaiterActionReservation
            locationName={reservation.locationAddress || "Kolkata, India"}
            locationId={"IN01"}
            date={reservation.date || dayjs().format("YYYY-MM-DD")}
            fromTime={
              reservation.timeSlot?.split(" - ")[0] || dayjs().format("HH:mm")
            }
            toTime={
              reservation.timeSlot?.split(" - ")[1] ||
              dayjs().add(1, "hour").format("HH:mm")
            }
            guests={parseInt(reservation.guestsNumber) || 1}
            tableNo={1}
            isEditReservationOpen={true}
            setReserveData={() => { }}
            reserveData={reservation}
            postpone={true}
            onClose={handleWaiterDialogClose}
            onGuestsChange={() => { }}
            onSuccess={handleWaiterSuccess}
          />
        </Box>
      </Modal>
    </>
  );
}

export default ReservationCard;
