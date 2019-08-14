import React from 'react';
import { Page } from '@choerodon/master';
import DeploymentHeader from './header';
import MainView from './main-view';
import { useResourceStore } from './stores';

export default function Resource() {
  const { permissions } = useResourceStore();

  return <Page service={permissions}>
    <DeploymentHeader />
    <MainView />
  </Page>;
}
