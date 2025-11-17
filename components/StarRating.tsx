
import React from 'react';

interface StarRatingProps {
  rating: number;
  onRatingChange?: (rating: number) => void;
  readOnly?: boolean;
}

const StarRating: React.FC<StarRatingProps> = ({ rating, onRatingChange, readOnly = false }) => {
  return (
    <div className="flex items-center" dir="ltr">
      {[1, 2, 3, 4, 5].map((star) => (
        <span
          key={star}
          className={`${readOnly ? '' : 'cursor-pointer'} text-2xl transition-colors duration-200`}
          onClick={() => onRatingChange && onRatingChange(star)}
          onMouseEnter={() => {}}
          onMouseLeave={() => {}}
        >
          <i className={`fa-solid fa-star ${rating >= star ? 'text-yellow-400' : 'text-gray-300 dark:text-gray-600'}`}></i>
        </span>
      ))}
    </div>
  );
};

export default StarRating;
