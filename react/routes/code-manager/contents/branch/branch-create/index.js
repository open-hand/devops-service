import React from 'react';
import { StoreProvider } from './store';
import BranchCluster from './branchCreate';

export default (props) => <StoreProvider {...props}>
  <BranchCluster />
</StoreProvider>;
