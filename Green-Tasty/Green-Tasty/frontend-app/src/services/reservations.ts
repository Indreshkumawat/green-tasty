import axios from 'axios';
//import Cookies from 'js-cookie';
import { type Reservation }  from '../interfaces/reservations';
import { type EditReservationParams } from '../interfaces/reservations';


const API_BASE_URL =  import.meta.env.VITE_APP_BASE_URL;


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



export const reservationService = {
    async getReservations(): Promise<Reservation[]> {
        const token = localStorage.getItem("authToken");
        try {
            const response = await axios.get(`${API_BASE_URL}/reservations`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                timeout: 60000
            });
            return response.data;
        } catch (error) {
            console.error('Error fetching reservations:', error);
            throw error;
        }
    },
    
    getWaiterReservations: async (queryString: string) => {
      const token = localStorage.getItem("authToken");
      console.log("queryString", queryString);
      console.log("token", token);
        const res = await axios.get(`${API_BASE_URL}/reservations?${queryString}`,
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                timeout: 60000
            }
        );
        return res.data;
      },
    
};



export const cancelReservation = async (id: string) => {
    const token = localStorage.getItem("authToken");
    try {
        const response = await axios.delete(`${API_BASE_URL}/reservations/${id}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error canceling reservations:', error);
        throw error;
    }
}

export const editReservation = async (id: string, params: EditReservationParams) => {
    const token = localStorage.getItem("authToken");
    const request = {
        guestsNumber: params.guestsNumber,
        timeFrom: params.timeFrom,
        timeTo: params.timeTo,
    }
    console.log("id",id, token, request);
    try {
      

      if (!token) {
        throw new Error('Login to Book the table');
      }
      
      const response = await fetch(`${API_BASE_URL}/reservations/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(request)
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to edit reservation');
      }
      
    }
    catch (error) {
      console.error('Error editing reservation:', error);
      return {
        data: null,
        error: error instanceof Error ? error.message : 'An unexpected error occurred'
      };
    }
  }

export const getReservationById = async (id: string, dishesType: string, sort: string) => {
    const token = localStorage.getItem("authToken");
    const params = new URLSearchParams();
    if (dishesType) params.append('dishType', dishesType);
    if (sort) params.append('sort', sort);
    try {
        const response = await axios.get(`${API_BASE_URL}/reservations/${id}/available-dishes?${params.toString()}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching reservation by id:', error);
        throw error;
    }
}

export const addCartItem = async (id: string, dishId: string) => {
    const token = localStorage.getItem("authToken");
    console.log("id", `${API_BASE_URL}/reservations/${id}/order/${dishId}`);
    try {
        const response = await axios.post(
            `${API_BASE_URL}/reservations/${id}/order/${dishId}`,
            {},  // empty body
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }
        );
        return response.data;
    } catch (error) {
        console.error('Error adding cart item:', error);
        throw error;
    }
}

export const makeWaiterReservation = async (
    params: MakeWaiterReservationParams
  ) => {
    try {
      const token = localStorage.getItem("authToken");
      if (!token) {
        throw new Error("Login to Book the table");
      }
      const response = await fetch(`${API_BASE_URL}/bookings/waiter`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
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
  