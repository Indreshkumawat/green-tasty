// features/cart/components/EditInProgressCartItem.tsx
import React, { useState } from "react";
import { useDispatch } from "react-redux";
import type { CartItem } from "../../interfaces/cart";
import {
  updateCartItem,
  cancelEditingCartItem,
  removeDishFromCart,
} from "../../redux/cartSlice";
import type { AppDispatch } from "../../redux/store";

interface EditInProgressCartItemProps {
  item: CartItem;
}

const EditInProgressCartItem: React.FC<EditInProgressCartItemProps> = ({
  item,
}) => {
  const dispatch = useDispatch<AppDispatch>();
  const [formData, setFormData] = useState({
    locationAddress: item.locationAddress,
    date: item.date,
    timeSlot: item.timeSlot,
  });

  const handleSave = () => {
    dispatch(
      updateCartItem({
        reservationId: item.reservationId,
        updates: {
          ...item,
          ...formData,
        },
      })
    );
  };

  const handleCancel = () => {
    dispatch(cancelEditingCartItem(item.reservationId));
  };

  const handleRemoveDish = (dishId: string) => {
    dispatch(
      removeDishFromCart({
        reservationId: item.reservationId,
        dishId,
      })
    );
  };

  return (
    <div className="edit-item">
      <h3>Editing Reservation</h3>

      <div className="edit-form">
        <label>
          Address:
          <input
            type="text"
            value={formData.locationAddress}
            onChange={(e) =>
              setFormData({ ...formData, locationAddress: e.target.value })
            }
          />
        </label>

        <label>
          Date:
          <input
            type="date"
            value={formData.date}
            onChange={(e) => setFormData({ ...formData, date: e.target.value })}
          />
        </label>

        <label>
          Time Slot:
          <select
            value={formData.timeSlot}
            onChange={(e) =>
              setFormData({ ...formData, timeSlot: e.target.value })
            }
          >
            <option value="12:00 - 13:00">12:00 PM - 1:00 PM</option>
            <option value="12:15 - 13:45">12:15 PM - 1:45 PM</option>
            <option value="18:00 - 19:30">6:00 PM - 7:30 PM</option>
          </select>
        </label>
      </div>

      <div className="dishes-list">
        {item.dishItems.map((dish) => (
          <div key={dish.dishId} className="dish-item">
            <img
              src={dish.dishImageUrl}
              alt={dish.dishName}
              width={50}
              height={50}
            />
            <span>
              {dish.dishName} - {dish.dishPrice} × {dish.dishQuantity}
            </span>
            <button onClick={() => handleRemoveDish(dish.dishId)}>
              Remove
            </button>
          </div>
        ))}
      </div>

      <div className="actions">
        <button onClick={handleSave} className="save-btn">
          Save Changes
        </button>
        <button onClick={handleCancel} className="cancel-btn">
          Cancel
        </button>
      </div>
    </div>
  );
};

export default EditInProgressCartItem;
