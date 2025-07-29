import React, { useState, useEffect } from "react";
import {
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  TextField,
  Popover,
  Box,
  CircularProgress,
  Typography,
} from "@mui/material";
import { useForm, Controller } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { CustomDateRangePicker } from "../components/CustomDateRangePicker";
import { getReport, locationInfo } from "../services/auth";
import ReportTable from "../components/ReportTable";
import ReportTableSkeleton from "../skeletons/reportTableSkeleton";
import { format } from "date-fns";
import { useSnackbar } from "notistack";

type Location = {
  address: string;
  locationId: string;
};

const schema = z.object({
  reportType: z.string().min(1, "Select a report type"), // Only required field
  dateRange: z.tuple([z.date().nullable(), z.date().nullable()]).optional(),
  location: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

function formatRange(range: [Date | null, Date | null]) {
  if (!range[0] || !range[1]) return "Select date range (optional)";
  return (
    format(range[0], "dd-MM-yyyy") + " - " + format(range[1], "dd-MM-yyyy")
  );
}

function formatDateForAPI(date: Date | null): string {
  return date ? format(date, "dd-MM-yyyy") : "";
}

function AdminPage() {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [locations, setLocations] = useState<Location[]>([]);
  const [loading, setLoading] = useState(true);
  const [report, setReport] = useState<any>(null);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [isReportGenerated, setIsReportGenerated] = useState(false);
  const [isGeneratingReport, setIsGeneratingReport] = useState(false);
  const { enqueueSnackbar } = useSnackbar();

  const {
    control,
    handleSubmit,
    formState: { errors },
    watch,
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      reportType: "staff_performance",
      dateRange: [null, null], // Default to empty
      location: "", // Default to empty
    },
  });

  useEffect(() => {
    const fetchLocations = async () => {
      try {
        const response = await locationInfo();
        if (response && Array.isArray(response)) {
          setLocations(response);
        } else {
          setLocationError("Failed to load locations");
          setLocations([]);
        }
      } catch (error) {
        console.error("Failed to load locations:", error);
        setLocationError("Failed to load locations");
        setLocations([]);
      } finally {
        setLoading(false);
      }
    };
    fetchLocations();
  }, []);

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const onSubmit = async (data: FormValues) => {
    const submitData = {
      reportType: data.reportType,
      fromDate: data.dateRange?.[0]
        ? formatDateForAPI(data.dateRange[0])
        : null,
      toDate: data.dateRange?.[1] ? formatDateForAPI(data.dateRange[1]) : null,
      locationId: data.location || null,
    };

    try {
      setIsGeneratingReport(true);
      const response = await getReport(submitData);
      setReport(response);
      setIsReportGenerated(true);
      if (!response?.content || response.content.length === 0) {
        enqueueSnackbar("No data found", { variant: "info" });
      } else {
        enqueueSnackbar("Report generated successfully", {
          variant: "success",
        });
      }
    } catch (error) {
      console.error("Error fetching report:", error);
      enqueueSnackbar("Error fetching report", { variant: "error" });
    } finally {
      setIsGeneratingReport(false);
    }
  };

  if (loading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        height="100vh"
      >
        <CircularProgress />
      </Box>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Grid container spacing={6} padding={4}>
        <Grid size={{ xs: 12, md: 3 }}>
          <FormControl fullWidth error={!!errors.reportType}>
            <InputLabel>Report Type *</InputLabel>
            <Controller
              name="reportType"
              control={control}
              render={({ field }) => (
                <Select {...field} label="Report Type *" required>
                  <MenuItem value="">Select report type</MenuItem>
                  <MenuItem value="staff_performance">
                    Staff Performance
                  </MenuItem>
                  <MenuItem value="Location_Performance">Sales</MenuItem>
                </Select>
              )}
            />
            {errors.reportType && (
              <Typography color="error" variant="caption">
                {errors.reportType.message}
              </Typography>
            )}
          </FormControl>
        </Grid>

        <Grid size={{ xs: 12, md: 3 }}>
          <Controller
            name="dateRange"
            control={control}
            render={({ field }) => (
              <>
                <TextField
                  fullWidth
                  label="Date Range (optional)"
                  value={formatRange(field.value || [null, null])}
                  onClick={handleOpen}
                  InputProps={{ readOnly: true }}
                />
                <Popover
                  open={Boolean(anchorEl)}
                  anchorEl={anchorEl}
                  onClose={handleClose}
                  anchorOrigin={{ vertical: "bottom", horizontal: "left" }}
                >
                  <Box p={2}>
                    <CustomDateRangePicker
                      value={field.value || [null, null]}
                      onChange={(range) => {
                        field.onChange(range);
                      }}
                    />
                  </Box>
                </Popover>
              </>
            )}
          />
        </Grid>

        <Grid size={{ xs: 12, md: 3 }}>
          <FormControl fullWidth error={!!errors.location || !!locationError}>
            <InputLabel>Location (optional)</InputLabel>
            <Controller
              name="location"
              control={control}
              render={({ field }) => (
                <Select {...field} label="Location (optional)">
                  <MenuItem value="">All Locations</MenuItem>
                  {locations.map((loc) => (
                    <MenuItem key={loc.locationId} value={loc.locationId}>
                      {loc.address}
                    </MenuItem>
                  ))}
                </Select>
              )}
            />
            {locationError && (
              <Typography color="error" variant="caption">
                {locationError}
              </Typography>
            )}
          </FormControl>
        </Grid>

        <Grid size={{ xs: 12, md: 3 }}>
          <Button
            fullWidth
            variant="contained"
            color="success"
            sx={{ height: "56px" }}
            type="submit"
          >
            Generate Report
          </Button>
        </Grid>

        {isReportGenerated && (
          <>
            <Typography variant="h6">Report</Typography>
            {isGeneratingReport ? (
              <Grid size={{ xs: 12 }}>
                <ReportTableSkeleton />
              </Grid>
            ) : report?.content?.length === 0 ? (
              <Typography variant="h6">No data found</Typography>
            ) : report?.content ? (
              <Grid size={{ xs: 12 }}>
                <ReportTable
                  rows={report.content}
                  reportType={watch("reportType")}
                />
              </Grid>
            ) : null}

            {report?.downloadLinkPDF &&
              report?.downloadLinkCSV &&
              !isGeneratingReport && (
                <Grid
                  size={{ xs: 12 }}
                  display="flex"
                  justifyContent="flex-end"
                  alignItems="flex-end"
                  marginRight={2}
                  marginTop={-6}
                >
                  <Select
                    fullWidth
                    displayEmpty
                    renderValue={() => "Download Report"}
                    sx={{ width: "200px" }}
                  >
                    <MenuItem
                      value="1"
                      onClick={() =>
                        window.open(`${report.downloadLinkPDF}`, "_blank")
                      }
                    >
                      Download in PDF
                    </MenuItem>
                    <MenuItem
                      value="2"
                      onClick={() =>
                        window.open(`${report.downloadLinkCSV}`, "_blank")
                      }
                    >
                      Download in CSV
                    </MenuItem>
                  </Select>
                </Grid>
              )}
          </>
        )}
      </Grid>
    </form>
  );
}

export default AdminPage;
