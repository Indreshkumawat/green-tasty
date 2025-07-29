import { Card, CardContent, Skeleton, Box, Chip, Grid } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";

export default function TableCardSkeleton() {
  return (
    <Grid container spacing={2} sx={{ mt: 2 }}>
      {Array.from({ length: 4 }).map((_, index) => (
        <Grid size={{ xs: 12, md: 6, sm: 12, lg: 6 }} key={index}>
          <Card sx={{ width: "100%", borderRadius: "16px" }}>
            {/* Image Skeleton */}
            <Skeleton
              variant="rectangular"
              width={200}
              height={250}
              sx={{ flexShrink: 0 }}
            />

            <Box sx={{ flex: 1, overflow: "hidden" }}>
              <CardContent>
                {/* Top row with address and table number */}
                <Box
                  sx={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    gap: 2,
                  }}
                >
                  <Skeleton variant="text" width="60%" height={24} />
                  <Skeleton variant="text" width="30%" height={24} />
                </Box>

                {/* Seating capacity */}
                <Skeleton
                  variant="text"
                  width="50%"
                  height={24}
                  sx={{ mt: 2 }}
                />

                {/* Slots available */}
                <Skeleton
                  variant="text"
                  width="70%"
                  height={24}
                  sx={{ mt: 2 }}
                />

                {/* Chips */}
                <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1, mt: 2 }}>
                  {[...Array(3)].map((_, index) => (
                    <Skeleton
                      key={index}
                      variant="rectangular"
                      width={200}
                      height={32}
                      sx={{ borderRadius: 2 }}
                    />
                  ))}

                  {/* Show all chip skeleton */}
                  <Chip
                    icon={<AddIcon color="disabled" />}
                    label={<Skeleton variant="text" width={60} />}
                    sx={{
                      width: 200,
                      height: 32,
                      border: "1px solid",
                      borderColor: "divider",
                      borderRadius: 2,
                      backgroundColor: "transparent",
                    }}
                  />
                </Box>
              </CardContent>
            </Box>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
}
