import { HiOutlineX } from "react-icons/hi";
import { Group } from "@mui/icons-material";
import { useEffect, useState } from "react";
import { makeWaiterReservation } from "../services/reservations";
import { type BookingDataProps } from "../interfaces/bookings";
import {
  Box,
  Typography,
  IconButton,
  TextField,
  MenuItem,
  Button,
  Paper,
  Stack,
} from "@mui/material";
import { HiOutlineLocationMarker } from "react-icons/hi";
import Radio from "@mui/material/Radio";
import RadioGroup from "@mui/material/RadioGroup";
import FormControlLabel from "@mui/material/FormControlLabel";
import Autocomplete from "@mui/material/Autocomplete";
import { useForm, Controller } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { customerInfo } from "../services/auth";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import { locationInfo } from "../services/auth";
import { format } from "date-fns";
import { enqueueSnackbar } from "notistack";

const tableOptions = [
  { value: "1", label: "Table 1" },
  { value: "2", label: "Table 2" },
  { value: "3", label: "Table 3" },
  { value: "4", label: "Table 4" },
  { value: "5", label: "Table 5" },
];

const timeSlots = [
  { start: "10:30", end: "12:00" },
  { start: "12:15", end: "13:45" },
  { start: "14:00", end: "15:30" },
  { start: "15:45", end: "17:15" },
  { start: "17:30", end: "19:00" },
  { start: "19:15", end: "20:45" },
  { start: "21:00", end: "22:30" },
];

const reservationSchema = z.object({
  locationName: z.string(),
  customerType: z.enum(["EXISTING", "VISITOR"]),
  customerName: z.string(),
  selectedCustomers: z.array(
    z.object({ label: z.string(), selected: z.boolean() })
  ),
  guests: z.number().min(1).max(10),
  fromTime: z.string(),
  toTime: z.string(),
  table: z.string(),
  date: z.string(),
  location: z
    .object({
      address: z.string(),
      locationId: z.string(),
    })
    .optional(),
});

interface NewReservationDialogProps
  extends Omit<BookingDataProps, "tableNo" | "onSuccess"> {
  tableNo?: number;
  isPostpone?: boolean;
  guests: number;
  setReserveData: (data: any) => void;
  onClose: () => void;
  onGuestsChange: (increment: boolean) => void;
  onSuccess: (data: {
    restaurantName: string;
    numberOfPeople: number;
    date: string;
    fromTime: string;
    toTime: string;
    tableNumber: string;
    location: string;
  }) => void;
}

const WaiterActionReservation: React.FC<NewReservationDialogProps> = ({
  locationName = "Kolkata, India",
  locationId = "IN01",
  tableNo,
  fromTime,
  toTime,
  guests: initialGuests,
  setReserveData,
  onClose,
  onGuestsChange,
  onSuccess,
  isEditReservationOpen,
  isPostpone,
}) => {
  const [isLoading, setIsLoading] = useState(false);
  const [table, setTable] = useState(tableOptions[0].value);
  const [localFromTime, setLocalFromTime] = useState(
    fromTime || timeSlots[0].start
  );
  const [localToTime, setLocalToTime] = useState(toTime || timeSlots[0].end);
  const [customers, setCustomers] = useState<
    Array<{ name: string; email: string }>
  >([]);
  const [selectedDate, setSelectedDate] = useState<Date>(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return today;
  });
  const [guests, setGuests] = useState(initialGuests);
  const [locations, setLocations] = useState<
    Array<{ address: string; locationId: string }>
  >([]);
  const [selectedLocation, setSelectedLocation] = useState<{
    address: string;
    locationId: string;
  } | null>(() => ({
    address: locationName,
    locationId: locationId,
  }));

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [customerData, locationData] = await Promise.all([
          customerInfo(),
          locationInfo(),
        ]);
        setCustomers(Array.isArray(customerData) ? customerData : []);

        const validLocations = Array.isArray(locationData) ? locationData : [];
        setLocations(validLocations);

        if (validLocations.length > 0) {
          setSelectedLocation(validLocations[0]);
          setValue("locationName", validLocations[0].address);
        }
      } catch (error) {
        console.error("Error loading data:", error);
        enqueueSnackbar("Failed to load customer and location data", {
          variant: "error",
        });
        setCustomers([]);
        setLocations([]);
      }
    };

    fetchData();
  }, []);

  const {
    control,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(reservationSchema),
    defaultValues: {
      locationName: selectedLocation?.address || "",
      customerType: "VISITOR",
      customerName: "",
      selectedCustomers: [],
      guests: initialGuests,
      fromTime: fromTime || timeSlots[0].start,
      toTime: toTime || timeSlots[0].end,
      table: tableOptions[0].value,
      date: format(new Date(), "yyyy-MM-dd"),
    },
  });

  const customerType = watch("customerType");

  const handleMakeReservation = async (formData: any) => {
    setIsLoading(true);

    const selectedCustomer = customers.find(
      (c) => c.name === formData.customerName
    );

    const formattedDate = format(selectedDate, "yyyy-MM-dd");

    const formatTime = (time: string) => {
      const [hours, minutes] = time.split(":");
      return `${hours.padStart(2, "0")}:${minutes.padStart(2, "0")}`;
    };

    const reservationData = {
      clientType: customerType === "EXISTING" ? "CUSTOMER" : "VISITOR",
      customerName:
        customerType === "VISITOR" ? "" : formData.customerName || "",
      customerEmail: selectedCustomer?.email || "",
      date: formattedDate,
      guestsNumber: guests.toString(),
      locationId: selectedLocation?.locationId || locationId,
      tableNumber: [(tableNo ?? table).toString()],
      timeFrom: formatTime(localFromTime),
      timeTo: formatTime(localToTime),
      time: formatTime(localFromTime),
    };

    try {
      const result = await makeWaiterReservation(reservationData);

      if (result.error) {
        enqueueSnackbar(
          result.error || "Time slot isn't valid or already booked",
          {
            variant: "error",
          }
        );
        return;
      }

      enqueueSnackbar(
        isPostpone
          ? "Reservation postponed successfully"
          : isEditReservationOpen
          ? "Reservation updated successfully"
          : "Reservation created successfully",
        { variant: "success" }
      );

      if (isEditReservationOpen || isPostpone) {
        onClose();
        return;
      }

      setReserveData(result.data);
      onSuccess({
        restaurantName: selectedLocation?.address || locationName,
        numberOfPeople: guests,
        date: format(selectedDate, "dd/MM/yyyy"),
        fromTime: localFromTime,
        toTime: localToTime,
        tableNumber: (tableNo || table).toString(),
        location: selectedLocation?.address || locationName,
      });
      onClose();
    } catch (err) {
      enqueueSnackbar("An error occurred while processing your reservation", {
        variant: "error",
      });
      console.error("Reservation error:", err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleGuestsChange = (increment: boolean) => {
    const newGuests = increment ? guests + 1 : guests - 1;
    if (newGuests >= 1 && newGuests <= 10) {
      setGuests(newGuests);
      onGuestsChange(increment);
      setValue("guests", newGuests);
    }
  };

  return (
    <Paper
      sx={{
        borderRadius: 4,
        p: 4,
        maxWidth: 400,
        mx: "auto",
        position: "relative",
      }}
      elevation={3}
    >
      <IconButton
        onClick={onClose}
        sx={{ position: "absolute", top: 16, right: 16 }}
      >
        <HiOutlineX size={24} />
      </IconButton>

      <Typography variant="h5" fontWeight={700} mb={3}>
        {isPostpone
          ? "Postpone Reservation"
          : isEditReservationOpen
          ? "Edit Reservation"
          : "New Reservation"}
      </Typography>

      <Box mb={2}>
        <LocalizationProvider dateAdapter={AdapterDateFns}>
          <Controller
            name="date"
            control={control}
            render={({ field }) => (
              <DatePicker
                label="Select Date"
                value={selectedDate}
                onChange={(newDate) => {
                  if (newDate) {
                    const date = newDate as Date;
                    setSelectedDate(date);
                    const formattedDate = format(date, "yyyy-MM-dd");
                    field.onChange(formattedDate);
                  }
                }}
                minDate={new Date()}
                format="dd/MM/yyyy"
                slotProps={{
                  textField: {
                    fullWidth: true,
                    error: !!errors.date,
                    helperText: errors.date?.message,
                  },
                }}
              />
            )}
          />
        </LocalizationProvider>
      </Box>

      <Box mb={2}>
        <Controller
          name="location"
          control={control}
          render={({ field }) => (
            <Autocomplete
              options={locations}
              getOptionLabel={(option) => option.address}
              value={selectedLocation}
              onChange={(_, newValue) => {
                setSelectedLocation(newValue);
                field.onChange(newValue);
                setValue("locationName", newValue?.address || "");
              }}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label={
                    <Box display="flex" alignItems="center">
                      <HiOutlineLocationMarker
                        style={{ marginRight: 8, color: "#00AD0C" }}
                      />
                      Location
                    </Box>
                  }
                  error={!!errors.location}
                  helperText={errors.location?.message}
                />
              )}
            />
          )}
        />
      </Box>

      <RadioGroup
        value={customerType}
        onChange={(e) =>
          setValue("customerType", e.target.value as "EXISTING" | "VISITOR")
        }
        sx={{ mb: 2 }}
      >
        <Stack direction="row" spacing={2}>
          <FormControlLabel
            value="VISITOR"
            control={
              <Radio
                sx={{
                  color: "#00AD0C",
                  "&.Mui-checked": { color: "#00AD0C" },
                }}
              />
            }
            label="Visitor"
            sx={{
              flex: 1,
              border: 2,
              borderColor: customerType === "VISITOR" ? "#00AD0C" : "#E0E0E0",
              borderRadius: 2,
              mx: 1,
              background: customerType === "VISITOR" ? "#F6FFF7" : "#fff",
            }}
          />
          <FormControlLabel
            value="EXISTING"
            control={
              <Radio
                sx={{
                  color: "#00AD0C",
                  "&.Mui-checked": { color: "#00AD0C" },
                }}
              />
            }
            label="Existing Customer"
            sx={{
              flex: 1,
              border: 2,
              borderColor: customerType === "EXISTING" ? "#00AD0C" : "#E0E0E0",
              borderRadius: 2,
              mx: 1,
              background: customerType === "EXISTING" ? "#F6FFF7" : "#fff",
            }}
          />
        </Stack>
      </RadioGroup>

      {customerType === "EXISTING" && (
        <Box mb={2}>
          <Controller
            name="customerName"
            control={control}
            render={({ field }) => (
              <Autocomplete
                options={customers.map((c) => c.name)}
                onChange={(_, newValue) => {
                  field.onChange(newValue);
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Select Customer"
                    error={!!errors.customerName}
                    helperText={errors.customerName?.message}
                  />
                )}
                value={field.value || null}
              />
            )}
          />
        </Box>
      )}

      <Box
        display="flex"
        alignItems="center"
        border={1}
        borderColor="#E0E0E0"
        borderRadius={2}
        px={2}
        py={1}
        mb={2}
        sx={{ opacity: isPostpone ? 0.5 : 1 }}
      >
        <Group sx={{ width: 20, height: 20, marginRight: 8 }} />
        <Typography flex={1}>Guests</Typography>
        <Button
          onClick={() => handleGuestsChange(false)}
          disabled={guests <= 1 || isPostpone}
          sx={{
            minWidth: 0,
            color: "#00AD0C",
            border: "1px solid #00AD0C",
            borderRadius: 2,
            px: 1,
            mx: 1,
          }}
        >
          -
        </Button>
        <Typography>{guests}</Typography>
        <Button
          onClick={() => handleGuestsChange(true)}
          disabled={guests >= 10 || isPostpone}
          sx={{
            minWidth: 0,
            color: "#00AD0C",
            border: "1px solid #00AD0C",
            borderRadius: 2,
            px: 1,
            mx: 1,
          }}
        >
          +
        </Button>
      </Box>

      <Typography fontWeight={600} mb={1}>
        Time
      </Typography>
      <Typography fontSize={14} color="text.secondary" mb={2}>
        Please choose your preferred time from the dropdowns below
      </Typography>
      <Stack direction="row" spacing={2} mb={2}>
        <TextField
          select
          label="From"
          value={localFromTime}
          onChange={(e) => {
            const start = e.target.value;
            setLocalFromTime(start);
            setValue("fromTime", start);
            const slot = timeSlots.find((slot) => slot.start === start);
            if (slot) {
              setLocalToTime(slot.end);
              setValue("toTime", slot.end);
            }
          }}
          fullWidth
          error={!!errors.fromTime}
          helperText={errors.fromTime?.message}
        >
          {timeSlots.map((opt) => (
            <MenuItem key={opt.start} value={opt.start}>
              {opt.start}
            </MenuItem>
          ))}
        </TextField>

        <TextField select label="To" value={localToTime} disabled fullWidth>
          <MenuItem value={localToTime}>{localToTime}</MenuItem>
        </TextField>
      </Stack>

      <TextField
        select
        label="Table"
        value={table}
        onChange={(e) => setTable(e.target.value)}
        fullWidth
        sx={{ mb: 3 }}
        error={!!errors.table}
        helperText={errors.table?.message}
      >
        {tableOptions.map((opt) => (
          <MenuItem key={opt.value} value={opt.value}>
            {opt.label}
          </MenuItem>
        ))}
      </TextField>

      <Button
        fullWidth
        variant="contained"
        sx={{
          background: "#00AD0C",
          color: "#fff",
          borderRadius: 2,
          py: 1.5,
          fontSize: 18,
          mt: 2,
          "&:hover": { background: "#00990A" },
        }}
        onClick={handleSubmit(handleMakeReservation)}
        disabled={isLoading}
      >
        {isLoading
          ? "Processing..."
          : isPostpone
          ? "Postpone Reservation"
          : isEditReservationOpen
          ? "Update Reservation"
          : "Make a Reservation"}
      </Button>
    </Paper>
  );
};

export default WaiterActionReservation;
