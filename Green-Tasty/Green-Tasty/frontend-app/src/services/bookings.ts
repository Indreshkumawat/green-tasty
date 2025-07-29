import {
  type Table,
  type TablesResponse,
} from "../interfaces/bookings";
const BASE_URL = import.meta.env.VITE_APP_BASE_URL;
//import Cookies from "js-cookie";

  interface MakeReservationParams {
    locationId: string;
    tableNumber: string[];
    date: string;
    guestsNumber: string;
    timeFrom: string;
    timeTo: string;
  }
  
  interface MakeWaiterReservationParams {
    clientType: string;
    customerName: string;
    customerEmail: string;
    date: string;
    guestsNumber: string;
    locationId: string;
    tableNumber: string[];
    timeFrom: string;
    timeTo: string;
    time: string;
  }
  
  export const getAvailableTables = async ({
    locationId,
    date,
    time,
    guests,
  }: {
    locationId: string;
    date: string;
    time: string;
    guests: string;
  }): Promise<{
    data: Table[] | null;
    error: string | null;
  }> => {
    try {
      const encodedTime = encodeURIComponent(time);
      const url = `${BASE_URL}/bookings/tables?locationId=${locationId}&date=${date}&time=${encodedTime}&guests=${guests}`;
      console.log(url);
      const response = await fetch(url);
  
      const contentType = response.headers.get("content-type");
      if (!response.ok) {
        if (contentType && contentType.includes("application/json")) {
          const errorData = await response.json();
          throw new Error(errorData.message || "Failed to fetch available tables");
        } else {
          const text = await response.text();
          throw new Error(`Non-JSON error response: ${text}`);
        }
      }
  
      const data: TablesResponse = await response.json();
      return { data: data.tables, error: null };
    } catch (error) {
      console.error("Error fetching available tables:", error);
      return {
        data: null,
        error:
          error instanceof Error ? error.message : "An unexpected error occurred",
      };
    }
  };
  
  
  
  
  
  export const makeReservation = async (params: MakeReservationParams) => {
    try {
      const accessToken = localStorage.getItem("authToken");
  
      if (!accessToken) {
        throw new Error("Login to Book the table");
      }
      const response = await fetch(`${BASE_URL}/bookings/client`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify(params),
      });
  
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Failed to make reservation");
      }
  
      const data = await response.json();
      return {
        data,
        error: null,
      };
    } catch (error) {
      console.error("Error making reservation:", error);
      return {
        data: null,
        error:
          error instanceof Error ? error.message : "An unexpected error occurred",
      };
    }
  };
  
  export const makeWaiterReservation = async (
    params: MakeWaiterReservationParams
  ) => {
    try {
      const accessToken = localStorage.getItem("token");
  
      if (!accessToken) {
        throw new Error("Login to Book the table");
      }
      const response = await fetch(`${BASE_URL}/bookings/waiter`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify(params),
      });
  
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Failed to make reservation");
      }
  
      const data = await response.json();
      return {
        data,
        error: null,
      };
    } catch (error) {
      console.error("Error making reservation:", error);
      return {
        data: null,
        error:
          error instanceof Error ? error.message : "An unexpected error occurred",
      };
    }
  };
  