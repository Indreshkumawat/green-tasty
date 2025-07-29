import {
  Avatar,
  Box,
  Button,
  Grid,
  TextField,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { getUser, updateUser } from "../services/auth";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { enqueueSnackbar } from "notistack";

const schema = z.object({
  firstName: z.string().min(1, "First Name is required"),
  lastName: z.string().min(1, "Last Name is required"),
});

type FormData = z.infer<typeof schema>;

function GeneralInformation() {
  const [previewImage, setPreviewImage] = useState<string>(
    "/images/profile.png"
  );

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    mode: "onChange",
  });

  const fetchUserData = async () => {
    try {
      const response = await getUser();
      const data = response.data;
      if (data) {
        setValue("firstName", data.firstName);
        setValue("lastName", data.lastName);
        setPreviewImage(data.imageUrl);
      }
    } catch (err) {
      console.error(err);
      enqueueSnackbar("Failed to fetch user data", { variant: "error" });
    }
  };

  const onSubmit = async (formData: FormData) => {
    try {
      const updatePayload: any = {
        firstName: formData.firstName,
        lastName: formData.lastName,
      };

      if (previewImage.startsWith("data:")) {
        updatePayload.base64encodedImage = previewImage.split(",")[1];
      }

      const response = await updateUser(updatePayload);
      if (response.error) {
        enqueueSnackbar(response.error, { variant: "error" });
      } else {
        enqueueSnackbar("User updated successfully", { variant: "success" });
        fetchUserData();
      }
    } catch (err) {
      console.error(err);
      enqueueSnackbar("Failed to update user", { variant: "error" });
    }
  };

  const handleImageUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewImage(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  useEffect(() => {
    fetchUserData();
  }, []);

  return (
    <Box sx={{ p: 4 }}>
      <Grid container spacing={4}>
        <Grid
          size={{ xs: 12, md: 6 }}
          sx={{ display: "flex", alignItems: "center" }}
        >
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              flexDirection: "column",
              mr: 2,
            }}
          >
            <Avatar src={previewImage} sx={{ width: 80, height: 80 }} />
            <input
              accept="image/*"
              type="file"
              id="profile-image-upload"
              onChange={handleImageUpload}
              style={{ display: "none" }}
            />
            <label htmlFor="profile-image-upload">
              <Button component="span" size="small">
                Edit
              </Button>
            </label>
          </Box>
          <Box>
            <Typography variant="h6">
              {/** Optional: show firstName + lastName here */}
            </Typography>
          </Box>
        </Grid>

        <Grid size={{ xs: 12, md: 12 }}>
          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  fullWidth
                  placeholder="First Name"
                  {...register("firstName")}
                  error={!!errors.firstName}
                  sx={{
                    "& .MuiOutlinedInput-root": {
                      borderRadius: "8px",
                    },
                  }}
                  helperText={errors.firstName?.message}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  fullWidth
                  placeholder="Last Name"
                  {...register("lastName")}
                  error={!!errors.lastName}
                  sx={{
                    "& .MuiOutlinedInput-root": {
                      borderRadius: "8px",
                    },
                  }}
                  helperText={errors.lastName?.message}
                />
              </Grid>
            </Grid>

            <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 4 }}>
              <Button
                type="submit"
                variant="outlined"
                sx={{ borderRadius: "10px" }}
              >
                Save Changes
              </Button>
            </Box>
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
}

export default GeneralInformation;
