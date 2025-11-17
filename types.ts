
export interface Tag {
  id: string;
  name: string;
  color: string;
}

export interface Parking {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
  address: string;
  notes: string;
  photos: string[]; // base64 encoded images
  timestamp: number;
  rating: number; // 1-5
  tags: string[]; // array of tag ids
  visits: number;
  lastVisited: number;
}
