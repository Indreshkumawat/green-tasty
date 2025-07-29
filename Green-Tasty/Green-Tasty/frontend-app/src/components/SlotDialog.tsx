import {
  Dialog,
  DialogTitle,
  DialogContent,
  Chip,
  Grid,
  IconButton,
  Typography,
  Box,
} from "@mui/material";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import CloseIcon from "@mui/icons-material/Close";
import { type Table } from "../interfaces/bookings";
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";
import { type Location } from "../interfaces/locations";
export const SlotsDialog = ({
  table,
  selectedLocation,
  open,
  setSelectedSlot,
  onClose,
}: {
  table: Table;
  selectedLocation: Location;
  open: boolean;
  setSelectedSlot: (slot: string) => void;
  onClose: () => void;
}) => {
  const { t } = useTranslation();
  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: {
          //height: 300,
          position: "relative",
          borderRadius: "16px",
        },
      }}
    >
      <DialogTitle>
        {t("available_slots")}
        <IconButton
          aria-label="close"
          onClick={onClose}
          sx={{
            position: "absolute",
            right: 8,
            top: 8,
            color: (theme) => theme.palette.grey[500],
          }}
        >
          <CloseIcon />
        </IconButton>
        <Box>
          <Typography variant="caption" sx={{ fontWeight: 500 }}>
            {t("there_are")} {table.availableSlots.length}{" "}
            {t("slots_available_for")} {selectedLocation.address}, {t("table")}{" "}
            {table.tableNumber},{" "}
            {dayjs(table.date, "DD-MM-YYYY").format("MMM DD, YYYY")}
          </Typography>
        </Box>
      </DialogTitle>

      <DialogContent>
        <Grid container spacing={2}>
          {table.availableSlots.map((slot, index) => (
            <Grid size={{ xs: 12, sm: 4, md: 4 }} key={index}>
              <Chip
                label={slot}
                icon={<AccessTimeIcon />}
                onClick={() => setSelectedSlot(slot)}
                sx={{
                  fontWeight: 500,
                  width: "100%",
                  backgroundColor: "transparent",
                  border: "1px solid",
                  borderColor: "primary.main",
                  borderRadius: 2,
                }}
              />
            </Grid>
          ))}
        </Grid>
      </DialogContent>
    </Dialog>
  );
};
