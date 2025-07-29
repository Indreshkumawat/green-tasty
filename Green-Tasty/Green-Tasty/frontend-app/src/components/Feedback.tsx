import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";

import Typography from "@mui/material/Typography";

import { type FeedbackInterface } from "../interfaces/feedback";
import { CardHeader } from "@mui/material";
import Avatar from "@mui/material/Avatar";
import Rating from "@mui/material/Rating";

export default function Feedback({
  feedback,
}: {
  feedback: FeedbackInterface;
}) {
  return (
    <Card
      sx={{
        maxWidth: "25%",
        borderRadius: "24px",
      }}
    >
      <CardHeader
        avatar={
          <Avatar
            sx={{
              bgcolor: "primary.main",
              fontSize: "large",
            }}
            src={feedback.userAvatarUrl}
            aria-label="testimonial"
          >
            {feedback.userName}
          </Avatar>
        }
        action={
          <Rating
            name="size-small"
            defaultValue={Number(feedback.serviceRating)}
            readOnly
            size="small"
          />
        }
        title={`${feedback.userName}`}
        subheader={feedback.date}
      />

      <CardContent>
        <Typography variant="body2" sx={{ color: "text.secondary" }}>
          {feedback.comment}
        </Typography>
      </CardContent>
    </Card>
  );
}
