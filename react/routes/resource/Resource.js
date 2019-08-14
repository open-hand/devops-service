import React from 'react';
import { Page } from '@choerodon/boot';
import DeploymentHeader from './header';
import MainView from './main-view';
import { useDeploymentStore } from './stores';

export default function Resource() {
  const { permissions } = useDeploymentStore();

  return <Page service={permissions}>
    <DeploymentHeader />
    <MainView />
  </Page>;
}
