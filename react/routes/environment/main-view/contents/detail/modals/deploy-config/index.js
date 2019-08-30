import React from 'react';
import { StoreProvider } from './stores';
import DeployConfig from './DeployConfig';

export default ({ modal, ...props }) => <StoreProvider {...props}>
  <DeployConfig modal={modal} />
</StoreProvider>;
