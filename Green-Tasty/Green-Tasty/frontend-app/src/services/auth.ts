import axios, { AxiosError } from "axios";
//import Cookies from "js-cookie";

const BASE_URL = import.meta.env.VITE_APP_BASE_URL;

export interface User {
  firstName: string;
  lastName: string;
  email?: string;
  imageUrl?: string;
  base64encodedImage?: string;
}

export interface ErrorResponse {
  message: string;
  status: number;
}

export const registerUser = async (firstName: string, lastName: string, email: string, password: string) => { 
  try {
    const response = await axios.post(`${BASE_URL}/auth/sign-up`, {
      firstName,
      lastName,
      email,
      password,
    });
    return response;
  } catch (error) {
    console.error("Error registering user:", error);
    throw error;
  }
};



export const loginUser = async (email: string, password: string) => {
    try {
        const response = await axios.post(`${BASE_URL}/auth/sign-in`, {
          email,
          password
        });
        return response;
      }
      catch (error) {
        if (error instanceof AxiosError) {
            const errorResponse = error.response?.data as ErrorResponse;
            return {
                data: null,
                error: errorResponse
            }
        }
      }
  
};

export const waiterProfile = async (waiterEmail: string, feedbackId: string) => {
  const token = localStorage.getItem("authToken");
  try {
    let fullUrl;
    if(feedbackId==""){
      fullUrl = `${BASE_URL}/waiters-info?waiterEmail=${encodeURIComponent(waiterEmail)}`;
    }else{
      fullUrl = `${BASE_URL}/waiters-info?waiterEmail=${encodeURIComponent(waiterEmail)}&feedbackId=${feedbackId}`;
    }
    const response = await axios.get(fullUrl, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response;
  } catch (error) {
    console.error("Error fetching waiter profile:", error);
    throw error;
  }
};


export const updateUser = async (user: User) => {
  const token = localStorage.getItem("authToken");
  try {
    console.log("user", user);
    console.log("token", token, BASE_URL);
    const response = await axios.put(`${BASE_URL}/users/profile`, user, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return {
      data: response.data,
      error: null,
    };
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<ErrorResponse>;
      if (axiosError.response) {
        return {
          data: null,
          error: axiosError.response.data.message || "Update failed",
        };
      }
    }
    return {
      data: null,
      error: "An unexpected error occurred",
    };
  }
};

export const getUser = async () => {
  const token = localStorage.getItem("authToken");
  try {
    console.log("Fetching profile with token:", token);

    const response = await axios.get(`${BASE_URL}/users/profile`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    console.log("Profile response:", response.data);

    // Ensure we have the required fields
    const profileData = {
      firstName: response.data.firstName || "",
      lastName: response.data.lastName || "",
      imageUrl: response.data.imageUrl || "",
    };

    return {
      data: profileData,
      error: null,
    };
  } catch (error) {
    console.error("Error fetching profile:", error);
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<ErrorResponse>;
      if (axiosError.response) {
        return {
          data: null,
          error: axiosError.response.data.message || "Failed to fetch profile",
        };
      }
    }
    return {
      data: null,
      error: "An unexpected error occurred",
    };
  }
};

export const changePassword = async (
  oldPassword: string,
  newPassword: string
) => {
  const token = localStorage.getItem("authToken");
  const response = await axios.put(
    `${BASE_URL}/users/profile/password`,
    { oldPassword, newPassword },
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.data;
};

export const locationInfo = async () => {
  const token = localStorage.getItem("authToken");
  try {
    const response = await axios.get(`${BASE_URL}/locations/select-options`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching loca  tion info:", error);
    return {
      data: null,
      error: "An unexpected error occurred",
    };
  }
};

export const customerInfo = async () => {
  const token = localStorage.getItem("authToken");
  try {
    const response = await axios.get(`${BASE_URL}/users/customer-info`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching customer info:", error);
    return {
      data: null,
      error: "An unexpected error occurred",
    };
  }
};

export const getReport = async (submitData: any) => {
  const token = localStorage.getItem("authToken");
  try {
    console.log("submitData", submitData);
    const response = await axios.get(
      `${BASE_URL}/reports`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
        params: {
          reportType: submitData.reportType,
          fromDate: submitData.fromDate,
          toDate: submitData.toDate,
          locationId: submitData.locationId,
        },
      }
    );
    return response.data;
  } catch (error) {
    console.error("Error fetching report:", error);
    return {
      data: null,
      error: "An unexpected error occurred",
    };
  }
};