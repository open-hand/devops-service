import React from 'react';
import { StoreProvider } from './stores';
import CreateCluster from './CreateCluster';

export default (props) => <StoreProvider {...props}>
  <CreateCluster />
</StoreProvider>;
