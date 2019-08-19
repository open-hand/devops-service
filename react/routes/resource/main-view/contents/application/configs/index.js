import React from 'react';
import { StoreProvider } from './stores';
import Configs from './Configs';

export default (props) => (
  <StoreProvider value={props}>
    <Configs />
  </StoreProvider>
);
