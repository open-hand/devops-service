import React from 'react';
import { StoreProvider } from './stores';
import NetworkContent from './NetworkContent';

export default () => (
  <StoreProvider>
    <NetworkContent />
  </StoreProvider>
);
