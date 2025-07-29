import {
  Card,
  CardContent,
  CardActions,
  Skeleton,
  Box,
  Chip,
  Grid,
} from "@mui/material";
import {
  Group,
  AccessTime,
  CalendarMonth,
  LocationOn,
} from "@mui/icons-material";

function ReservationCardSkeleton() {
  return (
    <Grid container spacing={2}>
      {Array.from({ length: 6 }).map((_, index) => (
        <Grid size={{ xs: 12, md: 4, sm: 6 }} key={index}>
          <Card
            sx={{
              width: "85%",
              display: "flex",
              flexDirection: "column",
              justifyContent: "space-between",

              borderRadius: 4,
              position: "relative",
              overflow: "visible",
              p: 2,
            }}
          >
            <CardContent
              sx={{ display: "flex", flexDirection: "column", gap: 2, p: 1 }}
            >
              {/* Location */}
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <LocationOn fontSize="medium" />
                <Skeleton variant="text" width="60%" height={24} />
              </Box>

              {/* Date */}
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <CalendarMonth fontSize="medium" />
                <Skeleton variant="text" width="50%" height={24} />
              </Box>

              {/* Time */}
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <AccessTime fontSize="medium" />
                <Skeleton variant="text" width="40%" height={24} />
              </Box>

              {/* Guests */}
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <Group fontSize="medium" />
                <Skeleton variant="text" width="30%" height={24} />
              </Box>
            </CardContent>

            {/* Fake chip */}
            <Chip
              label=""
              size="small"
              sx={{
                position: "absolute",
                top: 8,
                right: 8,
                backgroundColor: "#e0e0e0",
                color: "transparent",
                borderRadius: "999px",
                width: 80,
                height: 24,
              }}
            />

            <CardActions>
              <Box sx={{ ml: "auto", display: "flex", gap: 1 }}>
                <Skeleton
                  variant="rectangular"
                  width={90}
                  height={36}
                  sx={{ borderRadius: 2 }}
                />
                <Skeleton
                  variant="rectangular"
                  width={90}
                  height={36}
                  sx={{ borderRadius: 2 }}
                />
              </Box>
            </CardActions>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
}

export default ReservationCardSkeleton;
