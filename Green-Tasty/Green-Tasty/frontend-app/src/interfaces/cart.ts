// features/cart/types.ts
export interface DishItem {
    dishId: string;
    dishImageUrl: string;
    dishName: string;
    dishPrice: string;
    dishQuantity: number;
  }

  export interface CartItem {
    reservationId: string;
    date: string;
    content?: CartItem[];
    timeSlot: string;
    locationAddress: string;
    dishItems: DishItem[];
    state: 'UNSUBMITTED' | 'SUBMITTED' | 'EDIT_IN_PROGRESS';
  }
  
  export interface CartState {
    items: CartItem[];
    status: 'idle' | 'loading' | 'succeeded' | 'failed';
    error: string | null;
  }
  
  export interface UpdateCartItemPayload {
    reservationId: string;
    updates: Partial<CartItem>;
  }
  
  export interface RemoveDishPayload {
    reservationId: string;
    dishId: string;
  }
  
  export interface AddToCartPayload {
    reservationId: string;
    dish: DishItem;
  }