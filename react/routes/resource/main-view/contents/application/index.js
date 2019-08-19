import React from 'react';
import { StoreProvider } from './stores';
import AppContent from './AppContent';

export default (props) => (
  <StoreProvider value={props}>
    <AppContent />
  </StoreProvider>
);
