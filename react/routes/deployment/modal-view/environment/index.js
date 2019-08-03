import React from 'react';
import { StoreProvider } from './stores';
import EnvModal from './EnvModal';

export default props => (
  <StoreProvider {...props}>
    <EnvModal />
  </StoreProvider>
);
