import React from 'react';
import { StoreProvider } from './stores';
import Modals from './Modals';

export default (props) => (
  <StoreProvider {...props}>
    <Modals />
  </StoreProvider>
);
