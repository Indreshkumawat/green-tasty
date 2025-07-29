import {
  Box,
  DialogContent,
  TextField,
  Typography,
  Rating,
} from "@mui/material";
import { useTranslation } from "react-i18next";
import { Controller, type Control, type FieldErrors } from "react-hook-form";

type Props = {
  control: Control<any>;
  ratingName: string;
  commentName: string;
  errors: FieldErrors;
};

const CulinaryFeedback = ({
  control,
  ratingName,
  commentName,
  errors,
}: Props) => {
  const { t } = useTranslation();

  return (
    <DialogContent>
      <Typography variant="subtitle1" mb={1}>
        {t("How was the food?")}
      </Typography>

      <Box sx={{ my: 2 }}>
        <Controller
          name={ratingName}
          control={control}
          render={({ field }) => (
            <Rating
              {...field}
              value={field.value || 0}
              onChange={(_, value) => field.onChange(value)}
              size="large"
            />
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

export default CulinaryFeedback;
