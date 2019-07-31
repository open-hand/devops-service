import React from 'react';
import { StoreProvider } from './stores';
import TreeMenu from './TreeMenu';

export default props => (
  <StoreProvider {...props}>
    <TreeMenu />
  </StoreProvider>
);
