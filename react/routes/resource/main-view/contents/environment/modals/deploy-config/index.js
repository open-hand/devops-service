import React from 'react';
import { StoreProvider } from './stores';
import DeployConfigForm from './DeployConfigForm';

export default (props) => <StoreProvider {...props}>
  <DeployConfigForm />
</StoreProvider>;
