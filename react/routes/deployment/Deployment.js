import React from 'react';
import { Page, Header } from '@choerodon/boot';
import { Button } from 'choerodon-ui';
import DeploymentHeader from './header';
import MainView from './main-view';
import { useDeploymentStore } from './stores';

export default function Deployment() {
  const { permissions } = useDeploymentStore();
  
  return <Page service={permissions}>
    <DeploymentHeader />
    <MainView />
  </Page>;
}
