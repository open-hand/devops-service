import React from 'react';
import { StoreProvider } from './stores';
import Branch from './Branch';

export default (props) => <StoreProvider {...props}>
  <Branch />
</StoreProvider>;
