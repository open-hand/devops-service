import React from 'react';
import BuildNumber from './BuildNumber';
import { StoreProvider } from './stores';

export default (props) => (
  <StoreProvider {...props}>
    <BuildNumber />
  </StoreProvider>
);
