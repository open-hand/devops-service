import React from 'react';
import { StoreProvider } from './stores';
import DeployContent from './DeployContent';
import './index.less';

export default (props) => (
  <StoreProvider {...props}>
    <DeployContent />
  </StoreProvider>
);
