import {
  IconButton,
  DialogTitle,
  DialogContentText,
  Dialog,
  DialogActions,
  Button,
  Tab,
  Tabs,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import { useTranslation } from "react-i18next";
import { useEffect, useState } from "react";
import { waiterProfile } from "../services/auth";
import { enqueueSnackbar } from "notistack";
import { type Reservation } from "../interfaces/reservations";
import ServiceFeedback from "./serviceFeedback";
import CulinaryFeedback from "./culinaryFeedback";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { createFeedback, updateFeedback } from "../services/feedback";
import { type FeedbackInterface } from "../interfaces/feedback";
const feedbackSchema = z.object({
  serviceRating: z
    .number({ invalid_type_error: "Rating is required" })
    .min(1, "Rating is required"),
  serviceComment: z.string().min(1, "Comment is required"),
  otherRating: z
    .number({ invalid_type_error: "Rating is required" })
    .min(1, "Rating is required"),
  otherComment: z.string().min(1, "Comment is required"),
});

type FeedbackFormValues = z.infer<typeof feedbackSchema>;

const FeedbackDialog = ({
  reservation,
  onClose,
  isUpdateFeedback,
  waiterId,
  feedbackId,
  setRefresh,
}: {
  reservation: Reservation;
  onClose: () => void;
  isUpdateFeedback: boolean;
  waiterId: string;
  feedbackId: string;
  setRefresh: (refresh: boolean) => void;
}) => {
  const { t } = useTranslation();
  const [open, setOpen] = useState(true);
  const [waiter, setWaiter] = useState<any>(null);
  const [selectedTab, setSelectedTab] = useState("serviceFeedback");

  const {
    control,
    handleSubmit,
    setValue,
    watch,
    formState: { errors, isValid },
  } = useForm<FeedbackFormValues>({
    resolver: zodResolver(feedbackSchema),
    mode: "onChange",
    defaultValues: {
      serviceRating: 0,
      serviceComment: "",
      otherRating: 0,
      otherComment: "",
    },
  });

  const formValues = watch();
  console.log("Form Values:", formValues);
  console.log("Form Errors:", errors);
  console.log("Is Valid:", isValid);

  const onSubmit = async (data: FeedbackFormValues) => {
    // {
    //     "cuisineComment": "Good food",
    //     "cuisineRating": "4",
    //     "reservationId": "672846d5c951184d705b65d7",
    //     "serviceComment": "Good service, good atmosphere",
    //     "serviceRating": "5"
    //   }
    const submitData = {
      cuisineComment: data?.otherComment,
      cuisineRating: data.otherRating,
      reservationId: reservation.id,
      serviceComment: data?.serviceComment,
      serviceRating: data.serviceRating,
    };
    try {
      let feedback;
      if (isUpdateFeedback) {
        feedback = await updateFeedback(
          submitData as unknown as FeedbackInterface
        );
      } else {
        console.log("submitData", submitData);
        feedback = await createFeedback(
          submitData as unknown as FeedbackInterface
        );
      }
      if (feedback) {
        enqueueSnackbar(t("Feedback submitted successfully"), {
          variant: "success",
        });
        setOpen(false);
        onClose();
        setRefresh(true);
      }
    } catch (error) {
      console.error("Error submitting feedback:", error);
      enqueueSnackbar("Error submitting feedback", { variant: "error" });
    }
  };

  useEffect(() => {
    const fetchWaiterProfile = async () => {
      try {
        const response = await waiterProfile(waiterId, feedbackId);
        if (response.status === 200) {
          setWaiter(response.data);
          setValue("serviceComment", response.data.serviceComment || "");
        }
      } catch (error) {
        console.error("Error fetching waiter profile:", error);
        enqueueSnackbar("Waiter Not Exists", { variant: "error" });
      }
    };
    fetchWaiterProfile();
  }, [waiterId, feedbackId, setValue]);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: { borderRadius: "16px", minWidth: "552px", maxWidth: "566px" },
      }}
    >
      <DialogTitle fontSize="24px">
        {isUpdateFeedback ? t("update_feedback") : t("give_feedback")}
        <IconButton
          aria-label="close"
          onClick={() => {
            onClose();
          }}
          sx={{ position: "absolute", right: 8, top: 8 }}
        >
          <CloseIcon />
        </IconButton>
        <DialogContentText fontSize="14px">
          {t("please_rate_your_experience_below")}
        </DialogContentText>
      </DialogTitle>

      <Tabs
        value={selectedTab}
        onChange={(_, newValue) => setSelectedTab(newValue)}
        centered
      >
        <Tab label={t("Service Feedback")} value="serviceFeedback" />
        <Tab label={t("Culinary Feedback")} value="otherFeedback" />
      </Tabs>

      {selectedTab === "serviceFeedback" && (
        <ServiceFeedback
          waiter={waiter}
          control={control}
          ratingName="serviceRating"
          commentName="serviceComment"
          errors={errors}
        />
      )}

      {selectedTab === "otherFeedback" && (
        <CulinaryFeedback
          control={control}
          ratingName={"otherRating"}
          commentName={"otherComment"}
          errors={errors}
        />
      )}

      <DialogActions>
        <Button
          onClick={handleSubmit(onSubmit)}
          color="primary"
          variant="contained"
          fullWidth
          sx={{ borderRadius: "8px", height: "56px", m: 2, mt: 0 }}
          disabled={!isValid}
        >
          {t("Submit Feedback")}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default FeedbackDialog;
