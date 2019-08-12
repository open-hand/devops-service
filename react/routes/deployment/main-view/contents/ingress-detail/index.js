import React from 'react';
import { StoreProvider } from './stores';
import Content from './Content';

export default () => (
  <StoreProvider>
    <Content />
  </StoreProvider>
);
