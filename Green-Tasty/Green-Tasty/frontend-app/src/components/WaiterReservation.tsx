import AddIcon from "@mui/icons-material/Add";
import SearchIcon from "@mui/icons-material/Search";
import { useState, useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import {
  Grid,
  Pagination,
  Box,
  Snackbar,
  Alert,
  Select,
  MenuItem,
  FormControl,
  OutlinedInput,
  InputAdornment,
  Button,
  Modal,
} from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import TableRestaurantIcon from "@mui/icons-material/TableRestaurant";
import ReservationCardSkeleton from "../skeletons/reservationCardSkeleton";
import ReservationCard from "../components/reservationCard";
import NoReservation from "../components/noReservation";
import { type Reservation } from "../interfaces/reservations";
import { reservationService } from "../services/reservations";
import { useSnackbar } from "notistack";
import dayjs from "dayjs";
import WaiterActionReservation from "./waiterActionReservation";

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

const backgroundColor = "#f9f9f9";
const color = "#333";

export default function WaiterReservationPage() {
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(6);
  const [isLoading, setIsLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const { enqueueSnackbar } = useSnackbar();
  const [openDialog, setOpenDialog] = useState(false);
  const { control, getValues } = useForm({
    defaultValues: {
      date: dayjs(),
      timeSlot: "",
      tableNumber: "",
    },
  });

  const startIndex = (currentPage - 1) * pageSize;
  const paginatedReservations = reservations.slice(
    startIndex,
    startIndex + pageSize
  );
  const totalPages = Math.ceil(reservations.length / pageSize);

  const handleSearch = async () => {
    const { date, timeSlot, tableNumber } = getValues();

    const queryParams = new URLSearchParams();

    if (timeSlot) queryParams.append("time", timeSlot);
    if (date) queryParams.append("date", dayjs(date).format("DD-MM-YYYY"));
    if (tableNumber) queryParams.append("tableNumber", String(tableNumber));

    try {
      setIsLoading(true);
      const response = await reservationService.getWaiterReservations(
        queryParams.toString()
      );

      enqueueSnackbar("Reservations fetched successfully", {
        variant: "success",
      });
      setReservations(response);
      setCurrentPage(1);
    } catch (err) {
      enqueueSnackbar("Error fetching reservations", {
        variant: "error",
      });
      console.error("Error fetching reservations:", err);
      setError(err as Error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleReservationSuccess = () => {
    setOpenDialog(false);
    handleSearch(); // Refresh the reservations list
    enqueueSnackbar("Reservation created successfully", {
      variant: "success",
    });
  };

  useEffect(() => {
    handleSearch();
  }, [isRefreshing]);

  return (
    <Box p={2}>
      <Grid container spacing={2} mb={3}>
        {/* Date Filter */}
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Controller
            name="date"
            control={control}
            render={({ field }) => (
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <DatePicker
                  {...field}
                  disablePast
                  slotProps={{
                    textField: {
                      fullWidth: true,
                      variant: "outlined",
                      sx: {
                        bgcolor: backgroundColor,
                        borderRadius: "8px",
                        color: color,
                        "& .MuiInputBase-input": { color: color },
                        "& .MuiOutlinedInput-notchedOutline": {
                          borderColor: "rgba(0, 0, 0, 0.23)",
                        },
                        "&:hover .MuiOutlinedInput-notchedOutline": {
                          borderColor: "primary.main",
                        },
                        "& .MuiInputLabel-root": { color: color },
                        "& .MuiSvgIcon-root": { color: color },
                      },
                    },
                  }}
                />
              </LocalizationProvider>
            )}
          />
        </Grid>

        {/* Time Slot Filter */}
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Controller
            name="timeSlot"
            control={control}
            render={({ field }) => (
              <FormControl fullWidth>
                <Select
                  {...field}
                  displayEmpty
                  sx={{
                    bgcolor: backgroundColor,
                    borderRadius: "8px",
                    color,
                    "& .MuiSelect-icon": { color },
                    "& .MuiOutlinedInput-notchedOutline": {
                      borderColor: "rgba(0, 0, 0, 0.23)",
                    },
                    "&:hover .MuiOutlinedInput-notchedOutline": {
                      borderColor: "primary.main",
                    },
                  }}
                  input={
                    <OutlinedInput
                      startAdornment={
                        <InputAdornment position="start">
                          <AccessTimeIcon sx={{ color }} />
                        </InputAdornment>
                      }
                    />
                  }
                >
                  <MenuItem value="">
                    <em>Select Time Slot</em>
                  </MenuItem>
                  {timeSlots.map((timeSlot, index) => (
                    <MenuItem key={index} value={timeSlot}>
                      {timeSlot}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}
          />
        </Grid>

        {/* Table Number Filter */}
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Controller
            name="tableNumber"
            control={control}
            render={({ field }) => (
              <FormControl fullWidth>
                <Select
                  {...field}
                  displayEmpty
                  sx={{
                    bgcolor: backgroundColor,
                    borderRadius: "8px",
                    color,
                    "& .MuiSelect-icon": { color },
                    "& .MuiOutlinedInput-notchedOutline": {
                      borderColor: "rgba(0, 0, 0, 0.23)",
                    },
                    "&:hover .MuiOutlinedInput-notchedOutline": {
                      borderColor: "primary.main",
                    },
                  }}
                  input={
                    <OutlinedInput
                      startAdornment={
                        <InputAdornment position="start">
                          <TableRestaurantIcon sx={{ color }} />
                        </InputAdornment>
                      }
                    />
                  }
                >
                  <MenuItem value="">
                    <em>Select Table</em>
                  </MenuItem>
                  {[1, 2, 3, 4, 5].map((table) => (
                    <MenuItem key={table} value={table}>
                      {table}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}
          />
        </Grid>

        <Grid container spacing={2}>
          {/* Search Button */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <Button
              variant="contained"
              color="primary"
              sx={{ height: "100%", borderRadius: "8px" }}
              onClick={handleSearch}
            >
              <SearchIcon sx={{ mr: 1 }} /> Search
            </Button>
          </Grid>

          {/* Reservation Button */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <Button
              variant="contained"
              color="primary"
              sx={{ height: "100%", borderRadius: "8px" }}
              onClick={() => setOpenDialog(true)}
            >
              <AddIcon sx={{ mr: 1 }} /> Create
            </Button>
          </Grid>
        </Grid>
      </Grid>

      {/* Error Snackbar */}
      {error && (
        <Snackbar sx={{ mt: 2 }} open>
          <Alert severity="error">Error: {error.message}</Alert>
        </Snackbar>
      )}

      {/* <Box sx={{ display: "flex", justifyContent: "space-between", gap: 1 }}>
        <Typography variant="h5" gutterBottom>
          You have {reservations.length} reservations:
        </Typography>
        <Button
          variant="contained"
          color="primary"
          sx={{ borderRadius: 2, width: "24%", height: 50 }}
          onClick={() => setOpenDialog(true)}
        >
          Create Reservation
        </Button>
      </Box> */}

      {/* Reservation List */}
      {isLoading ? (
        <ReservationCardSkeleton />
      ) : reservations.length > 0 ? (
        <>
          <Grid container spacing={2}>
            {paginatedReservations.map((reservation) => (
              <Grid size={{ xs: 12, sm: 6, md: 4 }} key={reservation.id}>
                <ReservationCard
                  reservation={reservation}
                  setRefresh={setIsRefreshing}
                />
              </Grid>
            ))}
          </Grid>
          <Box mt={4} display="flex" justifyContent="center">
            <Pagination
              count={totalPages}
              page={currentPage}
              onChange={(_, value) => setCurrentPage(value)}
              color="primary"
            />
          </Box>
        </>
      ) : (
        <NoReservation />
      )}

      {/* New Reservation Dialog */}
      <Modal
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        sx={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <Box sx={{ outline: "none" }}>
          <WaiterActionReservation
            locationName="Kolkata, India"
            locationId="IN01"
            date={dayjs().format("YYYY-MM-DD")}
            fromTime={dayjs().format("HH:mm")}
            toTime={dayjs().add(1, "hour").format("HH:mm")}
            guests={1}
            tableNo={1}
            isEditReservationOpen={false}
            setReserveData={() => {}}
            reserveData={null}
            postpone={false}
            onClose={() => setOpenDialog(false)}
            onGuestsChange={() => {}}
            onSuccess={handleReservationSuccess}
          />
        </Box>
      </Modal>
    </Box>
  );
}
