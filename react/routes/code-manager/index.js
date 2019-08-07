import React from 'react';
import { StoreProvider } from './stores';
import CodeManager from './CodeManager';

export default props => (
  <StoreProvider {...props}>
    <CodeManager />
  </StoreProvider>
);
