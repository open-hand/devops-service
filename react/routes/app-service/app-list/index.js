import React from 'react';
import { StoreProvider } from './stores';
import AppList from './AppList';

export default props => (
  <StoreProvider {...props}>
    <AppList />
  </StoreProvider>
);
