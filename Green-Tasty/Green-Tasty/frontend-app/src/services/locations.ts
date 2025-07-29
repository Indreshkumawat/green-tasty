import axios from "axios";
const BASE_URL = import.meta.env.VITE_APP_BASE_URL;
export interface Location {
  address: string;
  locationId: string;
}
interface LocationsResponse {
  data: Location[] | null;
  error: string | null;
}
export const GetLocationsDropDown = async (): Promise<LocationsResponse> => {
  try {
    const response = await axios.get<Location[]>(`${BASE_URL}/locations/select-options`);
    return {
      data: response.data,
      error: null
    };
  } catch (error) {
    console.error('Error fetching locations:', error);
    return {
      data: null,
      error: 'Failed to fetch locations'
    };
  }
};

export const GetAllLocations = async () => {
    try {
        const response = await axios.get(`${BASE_URL}/locations`);
        return {
          data: response.data,
          error: null
        };
      } catch (error: any) {
        console.error('Error fetching locations:', error);
        throw error;
      }
}

export const GetLocationDetailsById = async (id : string) => {
    try {
        const response = await axios.get(`${BASE_URL}/locations/${id}`);
        return {
          data: response.data,
          error: null
        };
      } catch (error: any) {
        console.error('Error fetching locations:', error);
        throw error;
      }
}

export const GetLocationFeedback = async (
  id: string,
  type: string,
  sort: string = "rate,desc",
  page: number = 0,
  size: number = 6,
) => {
  console.log(id, type, sort, page, size);
  try {
    const response = await axios.get(`${BASE_URL}/locations/${id}/feedbacks`, {
      params: {
        type,
        page,
        size,
        sort,
      },
    });

    return {
      data: response.data,
      error: null,
    };
  } catch (error: any) {
    console.error("Error fetching location feedbacks:", error);
    throw error;
  }
};
