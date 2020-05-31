import React from 'react';
import { Permission } from '@choerodon/boot';
import { StoreProvider } from './stores';
import Detail from './Detail';

export default (props) => (
  <Permission
    service={[
      'choerodon.code.project.deploy.environment.ps.gitops',
      'choerodon.code.project.deploy.environment.ps.deploy',
      'choerodon.code.project.deploy.environment.ps.permission-allot',
    ]}
  >
    <StoreProvider {...props}>
      <Detail />
    </StoreProvider>
  </Permission>
);
