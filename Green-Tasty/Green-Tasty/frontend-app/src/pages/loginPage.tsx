import {
  Box,
  Grid,
  Typography,
  TextField,
  Button,
  Link,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  IconButton,
} from "@mui/material";
import { useForm, type SubmitHandler } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useNavigate } from "react-router-dom";
import Image from "../components/Image";
import { loginUser } from "../services/auth";
import { useSnackbar } from "notistack";
//import Cookies from "js-cookie";
import KeyIcon from "@mui/icons-material/Key";
import { useState } from "react";
import CloseIcon from "@mui/icons-material/Close";
import { useTranslation } from "react-i18next";
import { fetchCartItems } from "../redux/cartSlice"; // Add this import
import { useAppDispatch } from "../custom-hook/useRedux";

// Schema definition
const schema = z.object({
  email: z.string().email("Invalid email address").min(1, "Email is required"),
  password: z.string().min(8, "Password must be at least 8 characters long"),
});

type FormData = z.infer<typeof schema>;

function LoginPage() {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { enqueueSnackbar } = useSnackbar();
  const [openCredentials, setOpenCredentials] = useState(false);
  const dispatch = useAppDispatch();
  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    mode: "onChange",
  });

  const onSubmit: SubmitHandler<FormData> = async (data) => {
    try {
      const response = await loginUser(data.email, data.password);
      if (response?.data) {
        const { accessToken, role, username } = response.data;

        // Cookies.set("authToken", accessToken, {
        //   expires: 1,
        //   secure: true,
        //   sameSite: "strict",
        // });
        // Cookies.set("role", role.toLowerCase(), {
        //   expires: 1,
        //   secure: true,
        //   sameSite: "strict",
        // });
        // Cookies.set("username", username, {
        //   expires: 1,
        //   secure: true,
        //   sameSite: "strict",
        // });
        localStorage.setItem("authToken", accessToken);
        localStorage.setItem("role", role.toLowerCase());
        localStorage.setItem("username", username);
        enqueueSnackbar(t("Login successful! Welcome back!"), {
          variant: "success",
          anchorOrigin: { vertical: "top", horizontal: "center" },
        });

        const destination =
          role.toLowerCase() === "client"
            ? "/home"
            : role.toLowerCase() === "waiter"
            ? "/reservations"
            : "/admin";
        navigate(destination);
      } else if (response?.data === null) {
        const errorMessage =
          "error" in response && response?.error?.message
            ? response?.error?.message
            : t("Login failed. Please try again.");

        enqueueSnackbar(errorMessage, {
          variant: "error",
          anchorOrigin: { vertical: "top", horizontal: "center" },
        });
        throw new Error(errorMessage);
      }
    } catch (error: any) {
      const errorMessage =
        error?.response?.data?.message ||
        error?.message ||
        t("Login failed. Please try again.");

      enqueueSnackbar(errorMessage, {
        variant: "error",
        anchorOrigin: { vertical: "top", horizontal: "center" },
      });
      throw new Error(errorMessage);
    }
    try {
      await dispatch(fetchCartItems()).unwrap();
    } catch (cartError) {
      console.error("Failed to load cart:", cartError);
      enqueueSnackbar(t("Couldn't load your cart items"), {
        variant: "warning",
        anchorOrigin: { vertical: "top", horizontal: "center" },
      });
    }
  };

  return (
    <Grid
      container
      spacing={6}
      sx={{
        height: { xs: "auto", md: "100vh" },
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        mt: { md: 0, lg: 0 },
      }}
    >
      <Grid
        size={{ xs: 12, md: 6 }}
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          p: 2,
        }}
      >
        <Box
          component="form"
          noValidate
          onSubmit={handleSubmit(onSubmit)}
          sx={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            width: "100%",
            maxWidth: "496px",
            p: 3,
          }}
        >
          <Typography
            variant="body1"
            align="left"
            width="100%"
            sx={{ fontWeight: 300 }}
          >
            {t("WELCOME BACK")}
          </Typography>
          <Typography
            variant="h5"
            align="left"
            width="100%"
            sx={{ fontWeight: 500 }}
          >
            {t("Log In to Your Account")}
          </Typography>

          <TextField
            margin="normal"
            id="email"
            label={t("Email Address")}
            {...register("email")}
            error={!!errors.email}
            helperText={
              errors.email
                ? errors.email.message
                : t("e.g. username@domain.com")
            }
            fullWidth
            sx={{ "& .MuiOutlinedInput-root": { borderRadius: "8px" } }}
          />

          <TextField
            margin="normal"
            type="password"
            id="password"
            label={t("Password")}
            placeholder={t("Enter your password")}
            {...register("password")}
            error={!!errors.password}
            helperText={
              errors.password
                ? errors.password.message
                : t("Password is required")
            }
            fullWidth
            sx={{ "& .MuiOutlinedInput-root": { borderRadius: "8px" } }}
          />

          <Box
            sx={{
              width: "100%",
              display: "flex",
              justifyContent: "flex-start",
            }}
          >
            <Button
              variant="outlined"
              onClick={() => setOpenCredentials(true)}
              sx={{
                borderRadius: "6px",
                color: "primary.main",
                borderColor: "primary.main",
                "&:hover": { borderColor: "primary.main" },
              }}
              startIcon={<KeyIcon />}
            >
              {t("Credentials")}
            </Button>
          </Box>

          <Button
            type="submit"
            variant="contained"
            fullWidth
            disabled={!isValid}
            sx={{
              m: 1,
              borderRadius: "6px",
              height: "3rem",
              textTransform: "none",
            }}
          >
            {t("Log In")}
          </Button>

          <Grid size={{ xs: 12 }}>
            <Typography>
              {t("Don't have an account?")}{" "}
              <Link
                href="/register"
                variant="body2"
                sx={{
                  color: (theme) =>
                    theme.palette.mode === "dark" ? "#fff" : "#000",
                  textDecorationColor: (theme) =>
                    theme.palette.mode === "dark" ? "#fff" : "#000",
                }}
              >
                {t("CREATE NEW ACCOUNT")}
              </Link>
            </Typography>
          </Grid>
        </Box>
      </Grid>

      <Image />

      <Dialog
        open={openCredentials}
        onClose={() => setOpenCredentials(false)}
        PaperProps={{
          sx: {
            minWidth: { xs: "90%", sm: "552px" },
            borderRadius: "16px",
            border: "1px solid rgba(0, 0, 0, 0.12)",
            p: 3,
          },
        }}
      >
        <DialogTitle
          sx={{
            fontSize: "18px",
            fontWeight: "bold",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            p: 0,
          }}
        >
          {t("Credentials")}
          <IconButton
            aria-label="close"
            onClick={() => setOpenCredentials(false)}
            sx={{
              color: (theme) =>
                theme.palette.mode === "light" ? "grey.500" : "white",
            }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            <Box sx={{ fontWeight: "bold", color: "primary.main" }}>
              {t("Client Credentials")}:
            </Box>
            <br />
            {t("Email")}: username@domain.com
            <br />
            {t("Password")}: a@00119922A
            <br />
            <br />
            <Box sx={{ fontWeight: "bold", color: "primary.main" }}>
              {t("Waiter Credentials")}:
            </Box>
            <br />
            {t("Email")}: muthu@example.com
            <br />
            {t("Password")}: Muthu@123
            <Box sx={{ fontWeight: "bold", color: "primary.main" }}>
              {t("Admin Credentials")}:
            </Box>
            <br />
            {t("Email")}: krishnendu@example.com
            <br />
            {t("Password")}: Krish@123
          </DialogContentText>
        </DialogContent>
      </Dialog>
    </Grid>
  );
}

export default LoginPage;
