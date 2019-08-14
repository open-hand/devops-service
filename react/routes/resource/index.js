import React from 'react';
import { StoreProvider } from './stores';
import Resource from './Resource';

export default (props) => (
  <StoreProvider {...props}>
    <Resource />
  </StoreProvider>
);
