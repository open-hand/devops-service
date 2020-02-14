import React from 'react';
import { StoreProvider } from './stores';
import DeployTimes from './DeployTimes';

export default (props) => (
  <StoreProvider {...props}>
    <DeployTimes />
  </StoreProvider>
);
