export interface Reservation {
    visitorId: string;
    id: string;
    status: string;
    locationAddress: string;
    date: string;
    timeSlot: string;
    preOrder: string;
    guestsNumber: string;
    feedbackId: string;
    waiterEmail?: string;
    tableNo: string;
    tableIds?: string;
    waiterName?: string;
    customerName?: string;
  }

  export interface EditReservationParams {
    guestsNumber: string;
    timeFrom: string;
    timeTo: string;
}
