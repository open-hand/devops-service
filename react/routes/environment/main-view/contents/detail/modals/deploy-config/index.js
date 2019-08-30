import React from 'react';
import { StoreProvider } from './stores';
import DeployConfig from './DeployConfig';

export default (props) => <StoreProvider {...props}>
  <DeployConfig />
</StoreProvider>;
