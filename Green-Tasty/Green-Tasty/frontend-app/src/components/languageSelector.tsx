import * as React from "react";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import InputLabel from "@mui/material/InputLabel";
import OutlinedInput from "@mui/material/OutlinedInput";
import MenuItem from "@mui/material/MenuItem";
import FormControl from "@mui/material/FormControl";
import Select, { type SelectChangeEvent } from "@mui/material/Select";
//import Cookies from "js-cookie";
import IconButton from "@mui/material/IconButton";
import CloseIcon from "@mui/icons-material/Close";
import { useTranslation } from "react-i18next";

interface LanguageSelectorProps {
  open: boolean;
  setOpen: (open: boolean) => void;
}

const LanguageSelector: React.FC<LanguageSelectorProps> = ({
  open,
  setOpen,
}) => {
  const [language, setLanguage] = React.useState<string>("en");

  const { i18n, t } = useTranslation();

  React.useEffect(() => {
    setLanguage(localStorage.getItem("language") || "en");
    i18n.changeLanguage(localStorage.getItem("language") || "en");
  }, [localStorage.getItem("language"), i18n]);

  const handleChange = (event: SelectChangeEvent) => {
    const newLanguage = event.target.value as string;
    setLanguage(newLanguage);
    i18n.changeLanguage(newLanguage); // Change the language in i18next
  };

  const handleOk = () => {
    setOpen(false);
  };

  const handleClose = (_: React.SyntheticEvent<unknown>, reason?: string) => {
    if (reason !== "backdropClick") {
      setOpen(false);
    }
  };

  return (
    <Box>
      <Dialog
        disableEscapeKeyDown
        open={open}
        onClose={handleClose}
        sx={{
          "& .MuiDialog-paper": {
            borderRadius: "10px",
          },
        }}
      >
        <DialogTitle
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          {t("select_language")}
          <IconButton
            aria-label="close"
            onClick={handleClose}
            sx={{
              color: (theme) =>
                theme.palette.mode === "light" ? "grey.500" : "white",
              marginLeft: "auto",
            }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <Box component="form" sx={{ display: "flex", flexWrap: "wrap" }}>
            <FormControl
              sx={{
                mt: 1,
                minWidth: "552px",
                "& .MuiOutlinedInput-root": {
                  borderRadius: "10px",
                },
              }}
            >
              <InputLabel id="language-select-label">
                {t("select_language")}
              </InputLabel>
              <Select
                labelId="language-select-label"
                id="language-select"
                value={language}
                onChange={handleChange}
                input={<OutlinedInput label="Language" />}
                sx={{ height: 56 }}
              >
                {["en", "hi", "tel", "ua"].map((code, index) => (
                  <MenuItem key={index} value={code}>
                    {code === "en"
                      ? t("english")
                      : code === "hi"
                      ? t("hindi")
                      : code === "tel"
                      ? t("telugu")
                      : t("ukrainian")}
                    ({code})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button
            variant="contained"
            onClick={handleOk}
            disabled={!language}
            size="large"
            sx={{ fontWeight: "bold", borderRadius: "8px" }}
          >
            {t("ok")}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default LanguageSelector;
