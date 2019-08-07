import React from 'react';
import { StoreProvider } from './stores';
import Details from './Details';

export default props => (
  <StoreProvider {...props}>
    <Details />
  </StoreProvider>
);
