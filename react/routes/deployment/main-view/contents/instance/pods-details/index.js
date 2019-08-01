import React from 'react';
import { StoreProvider } from './stores';
import PodDetails from './PodDetails';

export default function Index(props) {
  return (
    <StoreProvider {...props}>
      <PodDetails />
    </StoreProvider>
  );
}
