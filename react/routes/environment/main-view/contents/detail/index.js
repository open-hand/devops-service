import React from 'react';
import { StoreProvider } from './stores';
import Detail from './Detail';

export default (props) => (
  <StoreProvider {...props}>
    <Detail />
  </StoreProvider>
);
