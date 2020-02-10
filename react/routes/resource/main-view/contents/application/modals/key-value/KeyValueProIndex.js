import React from 'react';
import { StoreProvider } from './stores';
import KeyValuePro from './KeyValuePro';

export default (props) => (
  <StoreProvider {...props}>
    <KeyValuePro />
  </StoreProvider>
);
