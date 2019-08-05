import React from 'react';
import { StoreProvider } from './stores';
import EnvContent from './EnvContent';

export default props => (
  <StoreProvider {...props}>
    <EnvContent />
  </StoreProvider>
);
