import StarIcon from "@mui/icons-material/Star";
import {
  Box,
  DialogContent,
  TextField,
  Typography,
  Rating,
  Avatar,
  Stack,
} from "@mui/material";
import { Controller, type Control, type FieldErrors } from "react-hook-form";
import { useTranslation } from "react-i18next";

type Props = {
  waiter: any;
  control: Control<any>;
  ratingName: string;
  commentName: string;
  errors: FieldErrors;
};

const ServiceFeedback = ({
  waiter,
  control,
  ratingName,
  commentName,
  errors,
}: Props) => {
  const { t } = useTranslation();
  console.log(waiter);
  return (
    <DialogContent>
      {waiter && (
        <Stack direction="row" alignItems="center" spacing={2} mb={2}>
          <Avatar src={waiter.imageUrl} alt={waiter.waiterName} />
          <Box>
            <Typography fontWeight={600}>{waiter.waiterName}</Typography>
            <Typography variant="body2" color="text.secondary">
              Waiter
            </Typography>
            <Stack direction="row" alignItems="center" spacing={0.5}>
              <Typography variant="body1" color="text.secondary">
                {waiter.waiterRating}
              </Typography>
              <StarIcon color="warning" fontSize="small" />
            </Stack>
          </Box>
        </Stack>
      )}

      <Box sx={{ display: "flex", alignItems: "center", width: "100%" }}>
        <Controller
          name={ratingName}
          control={control}
          render={({ field }) => (
            <Box sx={{ width: "100%" }}>
              <Rating
                {...field}
                value={field.value || 0}
                onChange={(_, value) => field.onChange(value)}
                size="large"
              />
              {errors[ratingName] && (
                <Typography color="error" fontSize="0.875rem" sx={{ mt: 0.5 }}>
                  {errors[ratingName]?.message as string}
                </Typography>
              )}
            </Box>
          )}
        />

        {errors[ratingName] && (
          <Typography color="error" fontSize="0.875rem">
            {errors[ratingName]?.message as string}
          </Typography>
        )}
      </Box>

      <Box sx={{ my: 2 }}>
        <Controller
          name={commentName}
          control={control}
          render={({ field }) => (
            <TextField
              label={t("Add your comments")}
              multiline
              rows={6}
              fullWidth
              {...field}
              error={!!errors[commentName]}
              helperText={errors[commentName]?.message as string}
              InputProps={{ sx: { borderRadius: "8px" } }}
            />
          )}
        />
      </Box>
    </DialogContent>
  );
};

export default ServiceFeedback;
