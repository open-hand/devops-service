import React from 'react';
import { StoreProvider } from './stores';
import BranchEdit from './BranchEdit';

export default (props) => <StoreProvider {...props}>
  <BranchEdit />
</StoreProvider>;