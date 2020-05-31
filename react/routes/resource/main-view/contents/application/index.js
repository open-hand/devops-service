import React from 'react';
import { Permission } from '@choerodon/boot';
import { StoreProvider } from './stores';
import AppContent from './AppContent';

export default (props) => (
  <Permission
    service={[
      'choerodon.code.project.deploy.app-deployment.resource.ps.network-tab',
      'choerodon.code.project.deploy.app-deployment.resource.ps.configmap-tab',
      'choerodon.code.project.deploy.app-deployment.resource.ps.cipher-tab',
    ]}
  >
    <StoreProvider value={props}>
      <AppContent />
    </StoreProvider>
  </Permission>
);
