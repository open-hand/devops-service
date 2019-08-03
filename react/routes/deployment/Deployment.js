import React from 'react';
import { Page } from '@choerodon/boot';
import MainView from './main-view';
import DeploymentHead from './modal-view';
import { useDeploymentStore } from './stores';

export default function Deployment() {
  const { permissions } = useDeploymentStore();

  return <Page service={permissions}>
    <DeploymentHead />
    <MainView />
  </Page>;
}
