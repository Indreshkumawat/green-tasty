import {
  Tab,
  Typography,
  Select,
  MenuItem,
  CircularProgress,
  Grid,
  Stack,
  Alert,
  Pagination,
  Rating,
} from "@mui/material";
import { Tabs } from "@mui/material";
import { Box } from "@mui/material";
import React, { useEffect, useState } from "react";
import { GetLocationFeedback } from "../services/locations";
import { type FeedbackTabsProps } from "../interfaces/feedbacktabs";
import { type TabPanelProps } from "../interfaces/tabpanel";
import { type FeedbackInterface } from "../interfaces/feedback";

const FeedbackTabs = ({ locationId }: FeedbackTabsProps) => {
  const [value, setValue] = useState(0); // Tab index
  const [sortBy, setSortBy] = useState<string>("rate,asc"); // Sorting criteria
  const [feedbacks, setFeedback] = useState<FeedbackInterface[]>([]); // Feedback data
  const [isLoading, setIsLoading] = useState(false); // Loading state
  const [error, setError] = useState<string | null>(null); // Error state
  const [type, setType] = useState<string>("SERVICE"); // Feedback type (Service or Cuisine)
  const [currentPage, setCurrentPage] = useState<number>(1); // Current page for pagination
  const [totalPages, setTotalPages] = useState<number>(1); // Total pages from API

  const handleChange = (_: React.SyntheticEvent, newValue: number) => {
    setValue(newValue);
    setCurrentPage(1); // Reset to first page when switching tabs
    setType(newValue === 0 ? "SERVICE" : "CUISINE"); // Update feedback type based on tab
  };

  const a11yProps = (index: number) => ({
    id: `simple-tab-${index}`,
    "aria-controls": `simple-tabpanel-${index}`,
  });

  const CustomTabPanel = (props: TabPanelProps) => {
    const { children, value, index, ...other } = props;

    return (
      <Box
        role="tabpanel"
        hidden={value !== index}
        id={`simple-tabpanel-${index}`}
        aria-labelledby={`simple-tab-${index}`}
        sx={{ mt: 2 }}
        {...other}
      >
        {children}
      </Box>
    );
  };

  // Fetch feedback data
  useEffect(() => {
    const fetchFeedback = async () => {
      try {
        setIsLoading(true);
        setError(null); // Clear previous errors
        const response = await GetLocationFeedback(locationId, type, sortBy, currentPage - 1); // Pass page number (0-based index)
        if (response.data) {
          setFeedback(response.data.content);
          setTotalPages(response.data.totalPages); // Set total pages for pagination
        } else {
          setFeedback([]);
          setTotalPages(1);
        }
      } catch (error: any) {
        const message =
          error?.response?.data?.message ||
          error.message ||
          "An error occurred";
        setError(message);
      } finally {
        setIsLoading(false);
      }
    };
    fetchFeedback();
  }, [locationId, sortBy, type, currentPage]);

  const handlePageChange = (_: React.ChangeEvent<unknown>, page: number) => {
    setCurrentPage(page); // Update current page
  };

  return (
    <>
      <Box sx={{ display: "flex" }}>
        <Tabs value={value} onChange={handleChange}>
          <Tab label="Service" {...a11yProps(0)} />
          <Tab label="Cuisine Experience" {...a11yProps(1)} />
        </Tabs>
        <Box flexGrow={1} />
        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
          <Typography>Sort By : </Typography>
          <Select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            sx={{ minWidth: 120, borderRadius: "8px" }}
          >
            <MenuItem value="rate,asc">Top Rated First</MenuItem>
            <MenuItem value="rate,desc">Lowest Rated First</MenuItem>
          </Select>
        </Box>
      </Box>
      <CustomTabPanel value={value} index={0}>
        {error ? (
          <Grid size={12}>
            <Stack sx={{ width: "100%" }}>
              <Alert variant="outlined" severity="error">
                {error}
              </Alert>
            </Stack>
          </Grid>
        ) : isLoading ? (
          <CircularProgress />
        ) : feedbacks.length > 0 ? (
          <>
            <Grid container spacing={2}>
              {feedbacks.map((feedback: FeedbackInterface) => (
                <Grid key={feedback.id}>
                  <Box
                    sx={{
                      border: "1px solid #ddd",
                      borderRadius: "8px",
                      padding: "16px",
                      display: "flex",
                      flexDirection: "column",
                      gap: 1,
                    }}
                  >
                    <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
                      <img
                        src={feedback.userAvatarUrl}
                        alt={feedback.userName}
                        style={{
                          width: "40px",
                          height: "40px",
                          borderRadius: "50%",
                        }}
                      />
                      <Typography variant="subtitle1">
                        {feedback.userName}
                      </Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary">
                      {feedback.comment}
                    </Typography>
                    <Rating
                      value={feedback.rate}
                      precision={0.1}
                      readOnly
                      size="small"
                    />
                    <Typography variant="caption" color="text.secondary">
                      {feedback.date}
                    </Typography>
                  </Box>
                </Grid>
              ))}
            </Grid>
            <Pagination
              count={totalPages}
              page={currentPage}
              onChange={handlePageChange}
              sx={{ mt: 2, display: "flex", justifyContent: "center" }}
            />
          </>
        ) : (
          <Stack sx={{ width: "100%" }}>
            <Alert variant="outlined" severity="info">
              No feedbacks found
            </Alert>
          </Stack>
        )}
      </CustomTabPanel>
      <CustomTabPanel value={value} index={1}>
        {error ? (
          <Grid size={12}>
            <Stack sx={{ width: "100%" }}>
              <Alert variant="outlined" severity="error">
                {error}
              </Alert>
            </Stack>
          </Grid>
        ) : isLoading ? (
          <CircularProgress />
        ) : feedbacks.length > 0 ? (
          <>
            <Grid container spacing={2}>
              {feedbacks.map((feedback: FeedbackInterface) => (
                <Grid key={feedback.id}>
                  <Box
                    sx={{
                      border: "1px solid #ddd",
                      borderRadius: "8px",
                      padding: "16px",
                      display: "flex",
                      flexDirection: "column",
                      gap: 1,
                    }}
                  >
                    <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
                      <img
                        src={feedback.userAvatarUrl}
                        alt={feedback.userName}
                        style={{
                          width: "40px",
                          height: "40px",
                          borderRadius: "50%",
                        }}
                      />
                      <Typography variant="subtitle1">
                        {feedback.userName}
                      </Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary">
                      {feedback.comment}
                    </Typography>
                    <Rating
                      value={feedback.rate}
                      precision={0.1}
                      readOnly
                      size="small"
                    />
                    <Typography variant="caption" color="text.secondary">
                      {feedback.date}
                    </Typography>
                  </Box>
                </Grid>
              ))}
            </Grid>
            <Pagination
              count={totalPages}
              page={currentPage}
              onChange={handlePageChange}
              sx={{ mt: 2, display: "flex", justifyContent: "center" }}
            />
          </>
        ) : (
          <Stack sx={{ width: "100%" }}>
            <Alert variant="outlined" severity="info">
              No feedbacks found
            </Alert>
          </Stack>
        )}
      </CustomTabPanel>
    </>
  );
};

export default FeedbackTabs;