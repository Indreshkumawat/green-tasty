// services/api/cartApi.ts
import axios from "axios";
//import Cookies from "js-cookie";
import type { CartItem } from "../interfaces/cart";

const API_BASE_URL = import.meta.env.VITE_APP_BASE_URL;

export const getCartItems = async (): Promise<{ content: CartItem[] }> => {
  try {
    const token = localStorage.getItem("authToken");
    const response = await axios.get(`${API_BASE_URL}/cart`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    console.log('Cart API Response:', response.data);
    return response.data;
  } catch (error) {
    console.error('Error fetching cart items:', error);
    return { content: [] };
  }
};

export const submitPreOrder = async (data: CartItem): Promise<CartItem> => {
  try {
    const token = localStorage.getItem("authToken");
    console.log("Submitting pre-order data:", data);
    const response = await axios.put(
      `${API_BASE_URL}/cart`,
      {
        ...data,
        state: 'SUBMITTED'
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
      }
    );
    console.log('Submit pre-order response:', response.data);
    return response.data;
  } catch (error: any) {
    console.error("Pre-order submission error:", error.response?.data || error.message);
    throw error;
  }
};
