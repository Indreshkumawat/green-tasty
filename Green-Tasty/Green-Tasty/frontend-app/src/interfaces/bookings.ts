export interface TimeSlot {
    availableSlots: string[];
  }
  
  export interface Table {
    date: string;
    capacity: string;
    availableSlots: string[];
    tableNumber: string;
    locationId: string;
  }
  
  export interface TablesResponse {
    tables: Table[];
  }
  
  export interface GetTablesParams {
    locationId: string;
    date: string;
    guests: number;
  }
  
  export interface BookingData {
    locationId: string;
    locationAddress: string;
    date: string;
    time: string;
    guests: number;
  }
  
  export interface BookingDataProps {
    locationName?: string;
    postpone?: boolean;
    locationId?: string;
    date?: string;
    fromTime?: string;
    toTime?: string;
    guests?: number;
    tableNo?: number;
    isEditReservationOpen?: boolean;
    setReserveData: (data: any) => void;
    reserveData: any;
    onClose: () => void;
    onGuestsChange: (increment: boolean) => void;
    onSuccess: (details: {
      restaurantName: string;
      numberOfPeople: number;
      date: string;
      fromTime: string;
      toTime: string;
      tableNumber: number;
      location: string;

    }) => void;
  }

