import React from 'react';
import { StoreProvider } from './stores';
import CipherContent from './CipherContent';

export default props => (
  <StoreProvider value={props}>
    <CipherContent />
  </StoreProvider>
);
