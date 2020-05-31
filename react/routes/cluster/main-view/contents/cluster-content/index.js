import React from 'react';
import { Permission } from '@choerodon/boot';
import { StoreProvider } from './stores';
import ClusterContent from './ClusterContent';

export default (props) => (
  <Permission
    service={[
      'choerodon.code.project.deploy.cluster.cluster-management.ps.cluster-detail',
    ]}
  >
    <StoreProvider {...props}>
      <ClusterContent />
    </StoreProvider>
  </Permission>
);
