import React from 'react';
import { StoreProvider } from './stores';
import DetailContent from './DetailContent';

export default props => (
  <StoreProvider {...props}>
    <DetailContent />
  </StoreProvider>
);
