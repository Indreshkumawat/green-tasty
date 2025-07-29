// features/cart/components/CartContainer.tsx
import React from "react";
import { useSelector } from "react-redux";
import type { RootState } from "../../redux/store.ts";
import { Drawer } from "@mui/material";
import UnsubmittedCartItem from "./UnsubmittedCartItem.tsx";
import SubmittedCartItem from "./SubmittedCartItem.tsx";
import EditInProgressCartItem from "./EditInProgressCartItem.tsx";

const CartContainer: React.FC<{
  open: boolean;
  setOpen: (open: boolean) => void;
}> = ({ open, setOpen }) => {
  const { items, status } = useSelector((state: RootState) => state.cart);

  if (status === "loading") {
    return <div>Loading...</div>;
  }

  // Debug logs
  // console.log(
  //   "Submitted items:",
  //   items[0].content?.filter((item) => item.state === "SUBMITTED")
  // );
  // console.log(
  //   "Unsubmitted items:",
  //   items[0].content?.filter((item) => item.state === "UNSUBMITTED")
  // );
  // console.log(
  //   "Edit in progress items:",
  //   items[0].content?.filter((item) => item.state === "EDIT_IN_PROGRESS")
  // );

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={() => setOpen(false)}
      PaperProps={{
        sx: {
          width: "50%",
        },
      }}
    >
      <div className="cart-container">
        <h1>Your Cart</h1>

        {/* UNSUBMITTED Items */}
        <div className="unsubmitted-section">
          <h2>Current Order</h2>
          {items
            .filter((item) => item.state === "UNSUBMITTED")
            .map((item) => (
              <UnsubmittedCartItem
                key={`unsubmitted-${item.reservationId}`}
                item={item}
              />
            ))}
        </div>

        {/* SUBMITTED Items */}
        <div className="submitted-section">
          <h2>Previous Orders</h2>
          {items
            .filter((item) => item.state === "SUBMITTED")
            .map((item) => (
              <SubmittedCartItem
                key={`submitted-${item.reservationId}`}
                item={item}
              />
            ))}
        </div>

        {/* EDIT_IN_PROGRESS Items */}
        <div className="edit-section">
          {items
            .filter((item) => item.state === "EDIT_IN_PROGRESS")
            .map((item) => (
              <EditInProgressCartItem
                key={`edit-${item.reservationId}`}
                item={item}
              />
            ))}
        </div>
      </div>
    </Drawer>
  );
};

export default CartContainer;
