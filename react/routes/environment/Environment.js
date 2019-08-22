import React from 'react';
import { Page } from '@choerodon/master';
import CustomHeader from '../../components/custom-header';
import MainView from './main-view';
import { useEnvironmentStore } from './stores';

export default function Environment() {
  const { permissions } = useEnvironmentStore();

  return <Page service={permissions}>
    <CustomHeader show />
    <MainView />
  </Page>;
}
