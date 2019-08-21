import React from 'react';
import { StoreProvider } from './stores';
import ClusterMainView from './MainView';

export default function ClusterMain(props) {
  return <StoreProvider {...props}>
    <ClusterMainView />
  </StoreProvider>;
}
