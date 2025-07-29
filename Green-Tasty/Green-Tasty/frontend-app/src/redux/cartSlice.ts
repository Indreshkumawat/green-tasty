// features/cart/cartSlice.ts
import { createAsyncThunk, createSlice, type PayloadAction } from '@reduxjs/toolkit';
import { getCartItems, submitPreOrder } from '../services/cart';
import type { RootState } from '../redux/store';
import type { DishItem, CartItem } from '../interfaces/cart';

// Types
interface CartState {
  items: CartItem[];
  status: 'idle' | 'loading' | 'succeeded' | 'failed';
  error: string | null;
}

// Initial state
const initialState: CartState = {
  items: [],
  status: 'idle',
  error: null
};

// Async Thunks
export const fetchCartItems = createAsyncThunk<CartItem[], void, { state: RootState }>(
  'cart/fetchCartItems',
  async () => {
    const response = await getCartItems();
    console.log('API Response:', response);
    // The API returns { content: CartItem[] }, so we need to return response.content
    return response.content;
  }
);

export const submitCartItem = createAsyncThunk<CartItem, CartItem, { state: RootState }>(
  'cart/submitCartItem',
  async (cartItem, { rejectWithValue }) => {
    try {
      console.log('Submitting cart item:', cartItem);
      // Ensure the item is marked as SUBMITTED before sending
      const itemToSubmit = {
        ...cartItem,
        state: 'SUBMITTED' as const
      };
      console.log('Item to submit:', itemToSubmit);
      const response = await submitPreOrder(itemToSubmit);
      console.log('Submit response:', response);
      return response;
    } catch (error: any) {
      console.error('Submit error:', error);
      return rejectWithValue(error.response?.data?.message || error.message || 'Failed to submit order');
    }
  }
);

const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {
    // Add new item to cart or update quantity if exists
    addToCart: (state, action: PayloadAction<{ reservationId: string; dish: DishItem }>) => {
      const { reservationId, dish } = action.payload;
      const existingItem = state.items.find(
        item => item.reservationId === reservationId && item.state === 'UNSUBMITTED'
      );

      if (existingItem) {
        const existingDish = existingItem.dishItems.find(d => d.dishId === dish.dishId);
        if (existingDish) {
          existingDish.dishQuantity += dish.dishQuantity || 1;
        } else {
          existingItem.dishItems.push(dish);
        }
      } else {
        state.items.push({
          reservationId,
          date: '',
          timeSlot: '',
          locationAddress: '',
          dishItems: [dish],
          state: 'UNSUBMITTED',
        });
      }
    },

    // Update existing cart item details
    updateCartItem: (state, action: PayloadAction<{ reservationId: string; updates: Partial<CartItem> }>) => {
      const { reservationId, updates } = action.payload;
      const itemIndex = state.items.findIndex(item => item.reservationId === reservationId);
      if (itemIndex !== -1) {
        state.items[itemIndex] = {
          ...state.items[itemIndex],
          ...updates
        };
      }
    },

    // Start editing a submitted item
    startEditingCartItem: (state, action: PayloadAction<string>) => {
      const reservationId = action.payload;
      const item = state.items.find(item => item.reservationId === reservationId);
      if (item && item.state === 'SUBMITTED') {
        item.state = 'EDIT_IN_PROGRESS';
      }
    },

    // Cancel editing and revert to submitted state
    cancelEditingCartItem: (state, action: PayloadAction<string>) => {
      const reservationId = action.payload;
      const item = state.items.find(item => item.reservationId === reservationId);
      if (item && item.state === 'EDIT_IN_PROGRESS') {
        item.state = 'SUBMITTED';
      }
    },

    // Remove a dish from a cart item
    removeDishFromCart: (state, action: PayloadAction<{ reservationId: string; dishId: string }>) => {
      const { reservationId, dishId } = action.payload;
      const cartItem = state.items.find(item => item.reservationId === reservationId);
      
      if (cartItem) {
        cartItem.dishItems = cartItem.dishItems.filter(dish => dish.dishId !== dishId);
        // Remove cart item if no dishes left
        if (cartItem.dishItems.length === 0) {
          state.items = state.items.filter(item => item.reservationId !== reservationId);
        }
      }
    },

    // Clear entire cart
    clearCart: (state) => {
      state.items = [];
    }
  },
  extraReducers: (builder) => {
    builder
      // Fetch cart items reducers
      .addCase(fetchCartItems.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchCartItems.fulfilled, (state, action) => {
        state.status = 'succeeded';
        console.log('Setting cart items:', action.payload);
        // The payload is already the content array from the API response
        state.items = action.payload;
      })
      .addCase(fetchCartItems.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message || 'Failed to fetch cart items';
      })
      
      // Submit cart item reducers
      .addCase(submitCartItem.pending, (state, action) => {
        state.status = 'loading';
        // Optimistically update the state
        const item = state.items.find(i => i.reservationId === action.meta.arg.reservationId);
        if (item) {
          console.log('Optimistically updating item state to SUBMITTED:', item);
          item.state = 'SUBMITTED';
        }
      })
      .addCase(submitCartItem.fulfilled, (state, action) => {
        state.status = 'succeeded';
        console.log('Submit fulfilled, updating item:', action.payload);
        // Replace the optimistic update with the server response
        const index = state.items.findIndex(item => item.reservationId === action.payload.reservationId);
        if (index !== -1) {
          state.items[index] = action.payload;
        } else {
          // If item not found, add it to the state
          state.items.push(action.payload);
        }
      })
      .addCase(submitCartItem.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload as string || 'Failed to submit cart item';
        // Revert optimistic update on failure
        const item = state.items.find(i => i.reservationId === action.meta.arg.reservationId);
        if (item) {
          console.log('Reverting item state to UNSUBMITTED:', item);
          item.state = 'UNSUBMITTED';
        }
      });
  }
});

// Export actions
export const { 
  addToCart,
  updateCartItem,
  startEditingCartItem,
  cancelEditingCartItem,
  removeDishFromCart,
  clearCart
} = cartSlice.actions;

// Selectors
export const selectCartItems = (state: RootState) => state.cart.items;
export const selectCartStatus = (state: RootState) => state.cart.status;
export const selectCartError = (state: RootState) => state.cart.error;
export const selectUnsubmittedItems = (state: RootState) => 
  state.cart.items.filter(item => item.state === 'UNSUBMITTED');
export const selectSubmittedItems = (state: RootState) => 
  state.cart.items.filter(item => item.state === 'SUBMITTED');
export const selectEditingItems = (state: RootState) => 
  state.cart.items.filter(item => item.state === 'EDIT_IN_PROGRESS');

export default cartSlice.reducer;