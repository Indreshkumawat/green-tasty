import axios from "axios";
import { type FeedbackInterface } from "../interfaces/feedback";
//import Cookies from "js-cookie";
const BASE_URL = import.meta.env.VITE_APP_BASE_URL;

export const createFeedback = async (feedback: FeedbackInterface) => {
  const token = localStorage.getItem("authToken");
  const response = await axios.post(
    BASE_URL + "/feedbacks",
    feedback,
    {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },

    }
  );
  return response.data;
};

export const updateFeedback = async (feedback: FeedbackInterface) => {
  const token = localStorage.getItem("authToken");
  const response = await axios.put(
    BASE_URL + "/feedbacks",
    feedback,
    {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.data;
};