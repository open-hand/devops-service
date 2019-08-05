import React from 'react';
import { StoreProvider } from './stores';
import Deployment from './Deployment';

export default props => (
  <StoreProvider {...props}>
    <Deployment />
  </StoreProvider>
);
