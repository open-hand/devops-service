import React from 'react';
import { StoreProvider } from './stores';
import ResourceSecurity from './ResourceSecurity';

export default (props) => (
  <StoreProvider {...props}>
    <ResourceSecurity />   
  </StoreProvider>
);
