import React from 'react';
import { StoreProvider } from './stores';
import Environment from './Environment';

export default (props) => (
  <StoreProvider {...props}>
    <Environment />
  </StoreProvider>
);
