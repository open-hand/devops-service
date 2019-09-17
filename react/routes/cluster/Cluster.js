import React from 'react';
import { Page } from '@choerodon/master';
import { useClusterStore } from './stores'; 
import MainView from './main-view';
import CustomHeader from '../../components/custom-header';

export default (props) => {
  const { permission } = useClusterStore();
  return (
    <Page service={permission}>
      <CustomHeader show />
      <MainView />
    </Page>
  );
};
