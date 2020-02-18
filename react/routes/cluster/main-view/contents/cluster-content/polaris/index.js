import React from 'react';
import { StoreProvider } from './stores';
import PolarisContent from './PolarisContent';

export default (props) => (
  <StoreProvider {...props}>
    <PolarisContent />
  </StoreProvider>
);
