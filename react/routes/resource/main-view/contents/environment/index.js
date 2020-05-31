import React from 'react';
import { Permission } from '@choerodon/boot';
import { StoreProvider } from './stores';
import EnvContent from './EnvContent';

export default props => (
  <Permission
    service={[
      'choerodon.code.project.deploy.app-deployment.resource.ps.gitops',
      'choerodon.code.project.deploy.app-deployment.resource.ps.deploy-config-tab',
      'choerodon.code.project.deploy.app-deployment.resource.ps.polaris']}
  >
    <StoreProvider {...props}>
      <EnvContent />
    </StoreProvider>
  </Permission>
);
