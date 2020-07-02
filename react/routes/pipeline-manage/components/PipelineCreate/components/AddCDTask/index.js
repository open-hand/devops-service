import React from 'react';
import { StoreProvider } from './stores';
import AddCDTask from './AddCDTask';

export default (props) => (
  <StoreProvider {...props}>
    <AddCDTask />
  </StoreProvider>
);
