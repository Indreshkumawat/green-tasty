export interface FeedbackInterface {
  rate: number | null | undefined;
  id: string;
  cuisineComment?: string;
  cuisineRating: string;
  reservationId: string;
  serviceComment?: string;
  serviceRating: string;
  locationId: string;
  userName: string;
  userEmail: string;
  userAvatarUrl: string;
  date: string;
  comment: string;
}
