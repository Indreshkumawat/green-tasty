import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import CardMedia from "@mui/material/CardMedia";
import Typography from "@mui/material/Typography";
import CardActionArea from "@mui/material/CardActionArea";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import { type Location } from "../interfaces/locations";
import CardActions from "@mui/material/CardActions";
import { useNavigate } from "react-router-dom";

export default function LocationDishCard(location: Location) {
  const navigate = useNavigate();
  return (
    <Card sx={{ maxWidth: "80%", height: "80%", borderRadius: "16px" }}>
      <CardActionArea
        onClick={() =>
          navigate("/restaurant-profile", {
            state: {
              location: location,
            },
          })
        }
      >
        <CardMedia
          component="img"
          height="140"
          image={location.imageUrl}
          alt="green iguana"
        />
        <CardContent>
          <Typography gutterBottom variant="h6" component="div">
            <LocationOnIcon
              sx={{ color: "primary.main", fontSize: "medium" }}
            />{" "}
            {location.address}
          </Typography>
          <Typography
            variant="body2"
            sx={{ color: "text.secondary", height: 30 }}
          >
            {location.description}
          </Typography>
        </CardContent>
      </CardActionArea>
      <CardActions
        sx={{
          display: "flex",
          justifyContent: "space-between",
          pr: 3,
          pl: 2,
          pb: 2,
        }}
      >
        <Typography variant="body1" sx={{ color: "text.secondary" }}>
          Total capacity: {location.totalCapacity}
        </Typography>
        <Typography variant="body1" sx={{ color: "text.secondary" }}>
          Average occupancy: {location.averageOccupancy}%
        </Typography>
      </CardActions>
    </Card>
  );
}
