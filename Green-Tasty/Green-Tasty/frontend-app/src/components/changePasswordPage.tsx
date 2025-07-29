import {
  Box,
  TextField,
  Button,
  Grid,
  IconButton,
  InputAdornment,
  Typography,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
} from "@mui/material";
import { FiEye, FiEyeOff } from "react-icons/fi";
import { useForm } from "react-hook-form";
import { useState, useEffect } from "react";

import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

import { enqueueSnackbar } from "notistack";

import { useTranslation } from "react-i18next";
import { changePassword } from "../services/auth";

type FormData = {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
};

const ChangePassword = () => {
  const { t } = useTranslation();

  const [showPassword, setShowPassword] = useState({
    old: false,
    new: false,
    confirm: false,
  });

  const [requirements, setRequirements] = useState({
    uppercase: false,
    lowercase: false,
    number: false,
    character: false,
    length: false,
    notMatchOld: true,
  });

  const schema = z
    .object({
      oldPassword: z
        .string()
        .min(8, t("Password must be at least 8 characters")),
      newPassword: z
        .string()
        .min(8, t("Password must be at least 8 characters")),
      confirmPassword: z.string(),
    })
    .refine((data) => data.newPassword === data.confirmPassword, {
      message: t("Passwords don't match"),
      path: ["confirmPassword"],
    });

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isValid },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    mode: "onChange",
  });

  // <-- FIX: default to empty string to avoid undefined errors
  const oldPassword = watch("oldPassword") ?? "";
  const newPassword = watch("newPassword") ?? "";
  const confirmPassword = watch("confirmPassword") ?? "";

  useEffect(() => {
    setRequirements({
      uppercase: /[A-Z]/.test(newPassword),
      lowercase: /[a-z]/.test(newPassword),
      number: /[0-9]/.test(newPassword),
      character: /[!@#$%^&*(),.?":{}|<>]/.test(newPassword),
      length: newPassword.length >= 8 && newPassword.length <= 16,
      notMatchOld: newPassword !== oldPassword && newPassword !== "",
    });
  }, [newPassword, oldPassword]);

  const isPasswordStrong = Object.values(requirements).every(Boolean);

  const onSubmit = async (data: FormData) => {
    console.log("data", data);
    try {
      await changePassword(data.oldPassword, data.newPassword);
      enqueueSnackbar(t("Password updated successfully!"), {
        variant: "success",
        anchorOrigin: { vertical: "top", horizontal: "center" },
      });
    } catch (error) {
      enqueueSnackbar(t("Error updating password:"), {
        variant: "error",
        anchorOrigin: { vertical: "top", horizontal: "center" },
      });
    }
  };

  const togglePasswordVisibility = (field: keyof typeof showPassword) => {
    setShowPassword((prev) => ({ ...prev, [field]: !prev[field] }));
  };

  return (
    <Grid container spacing={4}>
      <Grid size={{ xs: 12, md: 12 }}>
        <Box
          component="form"
          noValidate
          onSubmit={handleSubmit(onSubmit)}
          sx={{ maxWidth: 700 }}
        >
          <Grid container spacing={2}>
            {/* Old Password */}
            <Grid size={{ xs: 12, md: 12 }}>
              <TextField
                label={t("Current Password")}
                fullWidth
                type={showPassword.old ? "text" : "password"}
                {...register("oldPassword")}
                error={!!errors.oldPassword}
                helperText={
                  errors.oldPassword
                    ? errors.oldPassword.message
                    : t("Enter your current password")
                }
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={() => togglePasswordVisibility("old")}
                      >
                        {showPassword.old ? <FiEyeOff /> : <FiEye />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>

            {/* New Password */}
            <Grid size={{ xs: 12, md: 12 }}>
              <TextField
                label={t("New Password")}
                fullWidth
                type={showPassword.new ? "text" : "password"}
                {...register("newPassword")}
                error={!!errors.newPassword}
                helperText={
                  errors.newPassword
                    ? errors.newPassword.message
                    : t("At least one capital letter required")
                }
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={() => togglePasswordVisibility("new")}
                      >
                        {showPassword.new ? <FiEyeOff /> : <FiEye />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
              {newPassword && (
                <Box mt={1}>
                  <Typography
                    variant="caption"
                    color={isPasswordStrong ? "success.main" : "error.main"}
                  >
                    {isPasswordStrong
                      ? t("Strong password")
                      : t("Weak password")}
                  </Typography>
                  <List dense>
                    {[
                      { key: "uppercase", label: t("One uppercase letter") },
                      { key: "lowercase", label: t("One lowercase letter") },
                      { key: "number", label: t("One number") },
                      { key: "character", label: t("One special character") },
                      { key: "length", label: t("8-16 characters") },
                      {
                        key: "notMatchOld",
                        label: t("Not same as old password"),
                      },
                    ].map(({ key, label }) => (
                      <ListItem key={key} sx={{ py: 0 }}>
                        <ListItemIcon>
                          <span
                            style={{
                              width: 8,
                              height: 8,
                              borderRadius: "50%",
                              backgroundColor: requirements[
                                key as keyof typeof requirements
                              ]
                                ? "green"
                                : "red",
                              display: "inline-block",
                            }}
                          />
                        </ListItemIcon>
                        <ListItemText
                          primary={
                            <Typography
                              variant="caption"
                              color={
                                requirements[key as keyof typeof requirements]
                                  ? "success.main"
                                  : "error.main"
                              }
                            >
                              {label}
                            </Typography>
                          }
                        />
                      </ListItem>
                    ))}
                  </List>
                </Box>
              )}
            </Grid>

            {/* Confirm Password */}
            <Grid size={{ xs: 12, md: 12 }}>
              <TextField
                label={t("Confirm New Password")}
                fullWidth
                type={showPassword.confirm ? "text" : "password"}
                {...register("confirmPassword")}
                error={!!errors.confirmPassword}
                helperText={
                  errors.confirmPassword
                    ? errors.confirmPassword.message
                    : t("Passwords must match")
                }
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={() => togglePasswordVisibility("confirm")}
                      >
                        {showPassword.confirm ? <FiEyeOff /> : <FiEye />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
          </Grid>

          <Box display="flex" justifyContent="flex-end" mt={3}>
            <Button
              type="submit"
              variant="outlined"
              disabled={
                !isValid || !isPasswordStrong || confirmPassword !== newPassword
              }
              sx={{ borderRadius: "10px", width: { xs: "100%", md: "auto" } }}
            >
              {t("Save Changes")}
            </Button>
          </Box>
        </Box>
      </Grid>
    </Grid>
  );
};

export default ChangePassword;
