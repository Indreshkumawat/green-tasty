// hooks/useCart.ts
import { useEffect } from "react";
import { useAppDispatch, useAppSelector } from "./useRedux";
import { fetchCartItems } from "../redux/cartSlice";

export const useCart = () => {
  const dispatch = useAppDispatch();
  const { items, status, error } = useAppSelector(state => state.cart);

  const loadCart = () => {
    dispatch(fetchCartItems());
  };

  // Auto-load cart when hook is used (if not already loaded)
  useEffect(() => {
    if (status === 'idle') {
      loadCart();
    }
  }, [status, dispatch]);

  return {
    cartItems: items,
    isLoading: status === 'loading',
    error,
    refreshCart: loadCart
  };
};