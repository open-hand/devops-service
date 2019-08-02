import React from 'react';
import { StoreProvider } from './stores';
import Permissions from './Permissions';

export default props => (
  <StoreProvider {...props}>
    <Permissions />
  </StoreProvider>
);
