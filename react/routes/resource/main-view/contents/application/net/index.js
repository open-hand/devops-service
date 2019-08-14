import React from 'react';
import { StoreProvider } from './stores';
import Networking from './Networking';

export default () => (
  <StoreProvider>
    <Networking />
  </StoreProvider>
);
