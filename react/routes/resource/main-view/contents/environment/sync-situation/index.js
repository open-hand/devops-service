import React from 'react';
import { StoreProvider } from './stores';
import Situation from './Situation';

export default props => (
  <StoreProvider {...props}>
    <Situation />
  </StoreProvider>
);
