import React from 'react';
import { StoreProvider } from './stores';
import ClusterContent from './ClusterContent';

export default (props) => (
  <StoreProvider {...props}>
    <ClusterContent />
  </StoreProvider>
);
