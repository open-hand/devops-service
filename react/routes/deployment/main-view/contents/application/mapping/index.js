import React from 'react';
import { StoreProvider } from './stores';
import MappingContent from './MappingContent';

export default props => (
  <StoreProvider value={props}>
    <MappingContent />
  </StoreProvider>
);
