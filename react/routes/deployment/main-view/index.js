import React from 'react';
import { StoreProvider } from './stores';
import MainView from './MainView';

export default props => (
  <StoreProvider {...props}>
    <MainView />
  </StoreProvider>
);
