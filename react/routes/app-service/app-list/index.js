import React from 'react';
import { StoreProvider } from './stores';
import List from './List';

export default props => (
  <StoreProvider {...props}>
    <List />
  </StoreProvider>
);
