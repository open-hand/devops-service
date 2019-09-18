import React from 'react';
import { Page } from '@choerodon/master';
import { observer } from 'mobx-react-lite';
import { useClusterStore } from './stores'; 
import MainView from './main-view';
import CustomHeader from '../../components/custom-header';

export default observer((props) => {
  const { permission, clusterStore } = useClusterStore();
  return (
    <Page service={permission}>
      <CustomHeader show={clusterStore.getShowHeaderButton} />
      <MainView />
    </Page>
  );
});
