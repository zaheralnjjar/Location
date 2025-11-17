
import React from 'react';
import { Tag } from '../types';

interface TagPillProps {
  tag: Tag;
}

const TagPill: React.FC<TagPillProps> = ({ tag }) => {
  return (
    <span className={`px-2 py-1 text-xs font-semibold text-white rounded-full ${tag.color}`}>
      {tag.name}
    </span>
  );
};

export default TagPill;
