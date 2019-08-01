import React from 'react';
import { Page, Header } from '@choerodon/boot';
import { Button } from 'choerodon-ui';
import MainView from './main-view';
import { useDeploymentStore } from './stores';

export default function Deployment() {
  const { permissions } = useDeploymentStore();

  return <Page service={permissions}>
    <Header>
      <Button funcType="flat">删除</Button>
    </Header>
    <MainView />
  </Page>;
}
