import LocationOn from "@mui/icons-material/LocationOn";
import { IconButton, Typography, Rating, Button } from "@mui/material";
import { Box } from "@mui/material";

import { type Location } from "../interfaces/locations";
import { useNavigate } from "react-router-dom";

const LocationBanner = ({ location }: { location: Location }) => {
  const navigate = useNavigate();
  return (
    <>
      <Box>
        <Box sx={{ display: "flex", alignItems: "center" }}>
          <IconButton>
            <LocationOn fontSize="small" color="primary" />
          </IconButton>
          <Typography variant="body1" sx={{ fontWeight: "bold" }}>
            {location.address || "No address available"}
          </Typography>
          <Box flexGrow={1} />
          <Typography variant="body1" sx={{ fontWeight: "bold" }}>
            {location.rating || 0}
          </Typography>
          <Rating
            name="read-only"
            value={Number(location.rating) || 0}
            precision={0.5}
            readOnly
          />
        </Box>

        <Typography variant="body1" sx={{ fontWeight: "bold", mt: 2 }}>
          {location.description || "No description available"}
        </Typography>
      </Box>

      {/* Button fixed at bottom */}
      <Box sx={{ display: "flex", justifyContent: "center", mt: 4 }}>
        <Button
          variant="contained"
          color="primary"
          sx={{ width: "50%" }}
          onClick={() => navigate("/tables")}
        >
          Book Now
        </Button>
      </Box>
    </>
  );
};

export default LocationBanner;
