import { useEffect, useState } from "react";
import ReservationCard from "../components/reservationCard";
import { type Reservation } from "../interfaces/reservations";
import { reservationService } from "../services/reservations";
import { Grid, Pagination, Box, Snackbar, Alert } from "@mui/material";
import ReservationCardSkeleton from "../skeletons/reservationCardSkeleton";
import NoReservation from "../components/noReservation";
//import Cookies from "js-cookie";
import WaiterReservationPage from "../components/WaiterReservation";

export default function ReservationPage() {
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(6); // Number of reservations per page
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [refresh, setRefresh] = useState(false);

  const role = localStorage.getItem("role") || "";

  useEffect(() => {
    const fetchReservation = async () => {
      setIsLoading(true);
      try {
        const reservation = await reservationService.getReservations();
        setReservations(reservation);
      } catch (error) {
        setError(error as Error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchReservation();
  }, [refresh]);

  const startIndex = (currentPage - 1) * pageSize;
  const paginatedReservations = reservations.slice(
    startIndex,
    startIndex + pageSize
  );
  const totalPages = Math.ceil(reservations.length / pageSize);

  return role.toLowerCase() === "client" ? (
    <Box p={2}>
      {error && (
        <Snackbar sx={{ mt: 2 }}>
          <Alert severity="error">Error: {error.message}</Alert>
        </Snackbar>
      )}
      {isLoading ? (
        <ReservationCardSkeleton />
      ) : reservations.length > 0 ? (
        <>
          <Grid container spacing={2}>
            {paginatedReservations.map((reservation) => (
              <Grid size={{ xs: 12, md: 4, sm: 6 }} key={reservation.id}>
                <ReservationCard
                  reservation={reservation}
                  setRefresh={setRefresh}
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
    </Box>
  ) : (
    <WaiterReservationPage />
  );
}
