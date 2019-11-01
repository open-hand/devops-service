import React from 'react';
import { StoreProvider } from './stores';
import BranchEdit from './branchEdit';

export default (props) => <StoreProvider {...props}>
  <BranchEdit />
</StoreProvider>;
