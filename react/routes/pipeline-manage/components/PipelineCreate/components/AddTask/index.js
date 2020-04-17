import React from 'react';
import { StoreProvider } from './stores';
import AddTask from './AddTask';

export default (props) => (
  <StoreProvider {...props}>
    <AddTask />
  </StoreProvider>
);
