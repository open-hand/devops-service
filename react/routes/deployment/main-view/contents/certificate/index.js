import React from 'react';
import { StoreProvider } from './stores';
import CertContent from './CertContent';

export default () => (
  <StoreProvider>
    <CertContent />
  </StoreProvider>
);
