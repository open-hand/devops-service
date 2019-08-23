import React from 'react';
import { StoreProvider } from './stores';
import Group from './Group';

export default (props) => (
  <StoreProvider {...props}>
    <Group />
  </StoreProvider>
);
