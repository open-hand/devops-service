import React from 'react';
import { StoreProvider } from './stores';
import Content from './CodeQuality';

export default (props) => (
  <StoreProvider {...props}>
    <Content />
  </StoreProvider>
);
