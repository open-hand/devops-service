import React from 'react';
import { StoreProvider } from './stores';
import BranchCreate from './BranchCreate';

export default (props) => <StoreProvider {...props}>
  <BranchCreate />
</StoreProvider>;
