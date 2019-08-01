import React from 'react';
import { StoreProvider } from './stores';
import Cases from './Cases';

export default props => (
  <StoreProvider {...props}>
    <Cases />
  </StoreProvider>
);
