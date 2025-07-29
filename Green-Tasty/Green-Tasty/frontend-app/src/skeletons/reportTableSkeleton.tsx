import { Box, Skeleton } from "@mui/material";

export default function ReportTableSkeleton() {
  return (
    <Box sx={{ height: 500, width: "100%", p: 2 }}>
      {/* Header Skeleton */}
      <Box sx={{ display: "flex", mb: 2 }}>
        {[...Array(8)].map((_, index) => (
          <Skeleton
            key={index}
            variant="rectangular"
            width="100%"
            height={40}
            sx={{ mx: 0.5, flex: 1 }}
          />
        ))}
      </Box>

      {/* Row Skeletons */}
      {[...Array(5)].map((_, rowIndex) => (
        <Box key={rowIndex} sx={{ display: "flex", mb: 1 }}>
          {[...Array(8)].map((_, colIndex) => (
            <Skeleton
              key={colIndex}
              variant="rectangular"
              width="100%"
              height={52}
              sx={{ mx: 0.5, flex: 1 }}
            />
          ))}
        </Box>
      ))}
    </Box>
  );
}
