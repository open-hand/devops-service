import React from 'react';
import { Permission } from '@choerodon/boot';
import { NodeContentStoreProvider } from './stores';
import Content from './Content';

export default (props) => (
  <Permission
    service={['choerodon.code.project.deploy.cluster.cluster-management.ps.node']}
  >
    <NodeContentStoreProvider {...props}>
      <Content />
    </NodeContentStoreProvider>
  </Permission>
);
