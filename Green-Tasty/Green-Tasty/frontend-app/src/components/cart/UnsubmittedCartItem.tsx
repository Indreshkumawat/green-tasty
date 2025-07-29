// features/cart/components/UnsubmittedCartItem.tsx
import React from "react";
import { useDispatch } from "react-redux";
import type { CartItem } from "../../interfaces/cart";
import { submitCartItem } from "../../redux/cartSlice";
import type { AppDispatch } from "../../redux/store";

interface UnsubmittedCartItemProps {
  item: CartItem;
}

const UnsubmittedCartItem: React.FC<UnsubmittedCartItemProps> = ({ item }) => {
  const dispatch = useDispatch<AppDispatch>();

  const handleSubmit = () => {
    dispatch(submitCartItem(item));
  };

  return (
    <div className="border rounded-lg p-4 mb-4 bg-white shadow-sm">
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-lg font-semibold">Reservation Details</h3>
          <p className="text-gray-600">Date: {item.date}</p>
          <p className="text-gray-600">Time: {item.timeSlot}</p>
          <p className="text-gray-600">Address: {item.locationAddress}</p>
        </div>
        <button
          onClick={handleSubmit}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition-colors"
        >
          Submit Order
        </button>
      </div>
      <div>
        <h4 className="font-medium mb-2">Dishes:</h4>
        <ul className="space-y-2">
          {item.dishItems.map((dish) => (
            <li key={dish.dishId} className="flex justify-between items-center">
              <span>{dish.dishName}</span>
              <span className="text-gray-600">
                {dish.dishQuantity} x ${dish.dishPrice}
              </span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default UnsubmittedCartItem;
