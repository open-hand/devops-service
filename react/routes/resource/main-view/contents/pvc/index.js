import React from 'react';
import { StoreProvider } from './stores';
import PvcContent from './PvcContent';

export default (props) => (
  <StoreProvider {...props}>
    <PvcContent />
  </StoreProvider>
);
