import React from 'react';
import { StoreProvider } from './stores';
import CodeManager from './main-view';

export default (props) => (
  <StoreProvider {...props}>
    <CodeManager />
  </StoreProvider>
);
