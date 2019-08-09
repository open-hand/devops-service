import React from 'react';
import { StoreProvider } from './stores';
import IngressContent from './IngressContent';

export default () => (
  <StoreProvider>
    <IngressContent />
  </StoreProvider>
);
