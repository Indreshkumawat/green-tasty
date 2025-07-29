import axios from "axios";
const BASE_URL = import.meta.env.VITE_APP_BASE_URL;


export const GetPopularDishes = async () => {
    try {
        const response = await axios.get(`${BASE_URL}/dishes/popular`);
        return response.data;
      } catch (error: any) {
        console.error('Error fetching Dishes:', error);
        throw error;
      }
}


export const GetSpecialityDishes = async (id: string) => {
  try {
      const response = await axios.get(`${BASE_URL}/locations/${id}/speciality-dishes`);
      return {
        data: response.data,
        error: null
      };
    } catch (error) {
      console.error('Error fetching Dishes:', error);
      return {
        data: null,
        error: 'Failed to fetch Dishes'
      };
    }
}

export const FilterDishes = async (dishType?: string, sort?: string) => {
  try {
    const params = new URLSearchParams();
    if (dishType) params.append('dishType', dishType);
    if (sort) params.append('sort', sort);
    console.log(params.toString());
    const response = await axios.get(`${BASE_URL}/dishes?${params.toString()}`);
    return {
      data: response.data,
      error: null
    };
  } catch (error) {
    console.error('Error fetching Dishes:', error);
    return {
      data: null,
      error: 'Failed to fetch Dishes'
    };
  }
}

export const GetDishById = async (id: string) => {
  try {
    const response = await axios.get(`${BASE_URL}/dishes/${id}`);
    return {
      data: response.data,
      error: null
    };
  } catch (error) {
    console.error('Error fetching Dish:', error);
    return {
      data: null,
      error: 'Failed to fetch Dish'
    };
  }
}
