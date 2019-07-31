import React from 'react';
import { StoreProvider } from './stores';
import AssignPermissions from './AssignPermissions';

export default props => (
  <StoreProvider {...props}>
    <AssignPermissions />
  </StoreProvider>
);
