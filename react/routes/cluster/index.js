import React from 'react';
import ClusterManager from './Cluster';
import { StoreProvider } from './stores';


export default function Cluster(props) {
  return (
    <StoreProvider {...props}>
      <ClusterManager />
    </StoreProvider>
  );
}
