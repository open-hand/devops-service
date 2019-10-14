import React from 'react';
import { Page } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import { useClusterStore } from './stores';
import MainView from './main-view';
import CustomHeader from '../../components/custom-header';

export default observer(() => {
  const { permission, clusterStore } = useClusterStore();
  return (
    <Page service={permission}>
      <CustomHeader show={!clusterStore.getNoHeader} />
      <MainView />
    </Page>
  );
});
