import React from 'react';
import { StoreProvider } from './stores';
import TabContent from './TabContent';

export default (props) => (
  <StoreProvider {...props}>
    <TabContent />
  </StoreProvider>
);
