import React from 'react';
import { StoreProvider } from './stores';
import Deployment from './Deployment';

export default function Index(props) {
  return (
    <StoreProvider {...props}>
      <Deployment />
    </StoreProvider>
  );
}
