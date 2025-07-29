import React, { useEffect, useState } from "react";
import {
  Box,
  Button,
  Grid,
  MenuItem,
  Select,
  Typography,
  useTheme,
  FormControl,
  OutlinedInput,
  InputAdornment,
  IconButton,
} from "@mui/material";
import BannerImage from "../assets/ViewBanner.jpg";
import { useTranslation } from "react-i18next";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import GroupIcon from "@mui/icons-material/Group";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import { useForm, Controller } from "react-hook-form";
import {
  GetLocationDetailsById,
  GetLocationsDropDown,
} from "../services/locations";
import { type LocationDropDown } from "../interfaces/locationDropDown";
import dayjs, { Dayjs } from "dayjs";
import AddBoxIcon from "@mui/icons-material/AddBox";
import RemoveIcon from "@mui/icons-material/Remove";
import { getAvailableTables } from "../services/bookings";
import { useSnackbar } from "notistack";
import { type Table } from "../interfaces/bookings";
import { type Location } from "../interfaces/locations";
import LocationOnIcon from "@mui/icons-material/LocationOn";

type FormData = {
  locationId: string;
  date: Dayjs;
  timeSlot: string;
  numberOfPeople: number;
};

const TableViewBanner: React.FC<{
  setTables: (tables: Table[]) => void;
  setSelectedLocation: (location: Location) => void;
  setIsLoading: (isLoading: boolean) => void;
}> = ({ setTables, setSelectedLocation, setIsLoading }) => {
  const theme = useTheme();
  const { t } = useTranslation();
  const { enqueueSnackbar } = useSnackbar();
  const backgroundColor = theme.palette.mode === "dark" ? "black" : "white";
  const color = theme.palette.mode === "dark" ? "white" : "black";

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

  const [locations, setLocations] = useState<LocationDropDown[]>([]);

  // Function to get the next available time slot
  const getNextAvailableTimeSlot = () => {
    const currentTime = dayjs().format("HH:mm");
    const nextSlot = timeSlots.find((slot) => slot >= currentTime);
    return nextSlot || timeSlots[0]; // If no future slots today, return first slot
  };

  const { control, handleSubmit, setValue } = useForm<FormData>({
    defaultValues: {
      locationId: "",
      date: dayjs(),
      timeSlot: getNextAvailableTimeSlot(),
      numberOfPeople: 1,
    },
  });

  useEffect(() => {
    const fetchLocations = async () => {
      setIsLoading(true);
      const result = await GetLocationsDropDown();
      setIsLoading(false);
      if (result.data?.length) {
        setLocations(result.data);
        setValue("locationId", result.data[0].locationId);
      }
    };

    fetchLocations();
  }, [setValue, setIsLoading]);

  const onSubmit = async (data: FormData) => {
    const bookingData = {
      locationId: data.locationId,
      date: data.date.format("DD-MM-YYYY"),
      time: data.timeSlot,
      guests: data.numberOfPeople.toString(),
    };

    try {
      const response = await getAvailableTables(bookingData);
      if (response.data) {
        const locationDetails = await GetLocationDetailsById(data.locationId);
        setTables(response.data);
        setSelectedLocation(locationDetails.data);
        enqueueSnackbar(t("available_tables_found"), {
          variant: "success",
        });
      } else {
        enqueueSnackbar(response.error, {
          variant: "error",
        });
      }
    } catch (error) {
      console.error("Error fetching available tables:", error);
    }
  };

  return (
    <Grid sx={{ height: "400px", overflow: "hidden", marginTop: -4 }}>
      <Box sx={{ position: "relative", width: "100%", height: "100%" }}>
        <img
          src={BannerImage}
          alt="Banner"
          style={{
            width: "100%",
            height: "100%",
            objectFit: "cover",
            display: "block",
          }}
        />
        <Box
          sx={{
            position: "absolute",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            bgcolor: "rgba(0,0,0,0.4)",
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            alignItems: "start",
            textAlign: "center",
            color: "white",
            px: 2,
          }}
        >
          <Typography
            variant="h5"
            component="h1"
            sx={{
              fontWeight: "bold",
              color: theme.palette.primary.main,
              display: "flex",
              pl: 4,
            }}
          >
            {t("green_&_tasty_restaurants")}
          </Typography>
          <Typography
            variant="h4"
            component="h1"
            sx={{
              fontWeight: "bold",
              color: theme.palette.primary.main,
              display: "flex",
              pl: 4,
            }}
          >
            {t("book_a_table")}
          </Typography>

          <form onSubmit={handleSubmit(onSubmit)} style={{ width: "100%" }}>
            <Grid container spacing={4} sx={{ mt: 2, pl: 4, width: "100%" }}>
              {/* Location */}
              <Grid size={{ xs: 12, md: 3 }}>
                <Controller
                  name="locationId"
                  control={control}
                  render={({ field }) => (
                    <Select
                      {...field}
                      fullWidth
                      displayEmpty
                      sx={{
                        bgcolor: backgroundColor,
                        borderRadius: "8px",
                        color: color,
                      }}
                      input={
                        <OutlinedInput
                          startAdornment={
                            <InputAdornment position="start">
                              <LocationOnIcon sx={{ color: color }} />
                            </InputAdornment>
                          }
                        />
                      }
                    >
                      {locations.map((location) => (
                        <MenuItem
                          key={location.locationId}
                          value={location.locationId}
                        >
                          {location.address}
                        </MenuItem>
                      ))}
                    </Select>
                  )}
                />
              </Grid>

              {/* Date */}
              <Grid size={{ xs: 12, md: 2 }}>
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

                              input: { color: color },
                            },
                          },
                        }}
                      />
                    </LocalizationProvider>
                  )}
                />
              </Grid>

              {/* Time Slot */}
              <Grid size={{ xs: 12, md: 2 }}>
                <Controller
                  name="timeSlot"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <Select
                        sx={{
                          bgcolor: backgroundColor,
                          borderRadius: "8px",
                          color: color,
                        }}
                        {...field}
                        input={
                          <OutlinedInput
                            startAdornment={
                              <InputAdornment position="start">
                                <AccessTimeIcon
                                  sx={{ color: color }}
                                  fontSize="large"
                                />
                              </InputAdornment>
                            }
                          />
                        }
                      >
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

              {/* Number of People */}
              <Grid size={{ xs: 12, md: 2 }}>
                <Controller
                  name="numberOfPeople"
                  control={control}
                  render={({ field }) => (
                    <Box
                      sx={{
                        bgcolor: backgroundColor,
                        borderRadius: "8px",
                        width: "100%",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between", // Aligns items to the edges within the container
                        pl: 2,
                        pr: 1, // Add some padding on the right side to ensure no overflow
                        height: "100%",
                        boxSizing: "border-box", // Prevents overflow
                      }}
                    >
                      <GroupIcon sx={{ color: color }} fontSize="large" />
                      <Box
                        sx={{
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          flexGrow: 1,
                        }}
                      >
                        <IconButton
                          onClick={() => field.onChange(field.value - 1)}
                          disabled={field.value === 1}
                          sx={{
                            padding: "4px", // Reduces icon button padding to prevent overflow
                          }}
                        >
                          <RemoveIcon sx={{ color: color }} fontSize="large" />
                        </IconButton>
                        <Typography variant="h6" color={color}>
                          {field.value}
                        </Typography>
                        <IconButton
                          onClick={() => field.onChange(field.value + 1)}
                          disabled={field.value === 5}
                          sx={{
                            padding: "4px",
                          }}
                        >
                          <AddBoxIcon sx={{ color: color }} fontSize="large" />
                        </IconButton>
                      </Box>
                    </Box>
                  )}
                />
              </Grid>

              {/* Submit Button */}
              <Grid size={{ xs: 12, md: 2 }}>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  fullWidth
                  sx={{
                    width: "100%",
                    height: "100%",
                    borderRadius: "8px",
                  }}
                >
                  {t("book_a_table")}
                </Button>
              </Grid>
            </Grid>
          </form>
        </Box>
      </Box>
    </Grid>
  );
};

export default TableViewBanner;
