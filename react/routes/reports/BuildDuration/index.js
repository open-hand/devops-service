import React from 'react';
import BuildDuration from './BuildDuration';
import { StoreProvider } from './stores';

export default (props) => (
  <StoreProvider {...props}>
    <BuildDuration />
  </StoreProvider>
);
