import React from 'react';
import { StoreProvider } from './stores';
import InstanceContent from './InstanceContent';

export default props => (
  <StoreProvider {...props}>
    <InstanceContent />
  </StoreProvider>
);
