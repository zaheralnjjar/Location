
import { Tag } from './types';

export const PREDEFINED_TAGS: Tag[] = [
  { id: 'mall', name: 'مول', color: 'bg-blue-500' },
  { id: 'hospital', name: 'مستشفى', color: 'bg-red-500' },
  { id: 'airport', name: 'مطار', color: 'bg-yellow-500' },
  { id: 'work', name: 'عمل', color: 'bg-green-500' },
  { id: 'home', name: 'منزل', color: 'bg-purple-500' },
];

export const LOCAL_STORAGE_KEY_PARKINGS = 'advancedParkingTrackerParkings';
export const LOCAL_STORAGE_KEY_TAGS = 'advancedParkingTrackerTags';
