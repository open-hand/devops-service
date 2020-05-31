import React from 'react';
import { Permission } from '@choerodon/boot';
import { StoreProvider } from './stores';
import InstanceContent from './InstanceContent';

export default (props) => (
  <Permission
    service={[
      'choerodon.code.project.deploy.app-deployment.resource.ps.events-tab',
      'choerodon.code.project.deploy.app-deployment.resource.ps.runningdetail-tab',
      'choerodon.code.project.deploy.app-deployment.resource.ps.poddetail-tab',
    ]}
  >
    <StoreProvider {...props}>
      <InstanceContent />
    </StoreProvider>
  </Permission>
);
