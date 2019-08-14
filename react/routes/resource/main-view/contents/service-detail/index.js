import React from 'react';
import { StoreProvider } from './stores';
import ServiceDetail from './ServiceDetail';

export default () => (
  <StoreProvider>
    <ServiceDetail />
  </StoreProvider>
);
