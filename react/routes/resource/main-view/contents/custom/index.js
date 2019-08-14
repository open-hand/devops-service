import React from 'react';
import { StoreProvider } from './stores';
import CustomContent from './CustomContent';

export default () => (
  <StoreProvider>
    <CustomContent />
  </StoreProvider>
);
