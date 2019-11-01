import React from 'react';
import { StoreProvider } from './store';
import BranchCreate from './branchCreate';

export default (props) => <StoreProvider {...props}>
  <BranchCreate />
</StoreProvider>;
