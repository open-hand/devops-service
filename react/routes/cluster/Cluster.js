import React from 'react';
import { Page } from '@choerodon/master';
import { useClusterStore } from './stores'; 
import Header from './header';
import MainView from './main-view';

export default (props) => {
  const { permission } = useClusterStore();
  return (
    <Page service={permission}>
      <Header />
      <MainView />
    </Page>
  );
};
