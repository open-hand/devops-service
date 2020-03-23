import React from 'react';
import { StoreProvider } from './stores';
import Content from './Content';

export default (props) => (
  <StoreProvider {...props}>
    <Content />
  </StoreProvider>
);
