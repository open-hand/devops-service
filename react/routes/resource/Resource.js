import React from 'react';
import { observer } from 'mobx-react-lite';
import { Page } from '@choerodon/master';
import CustomHeader from '../../components/custom-header';
import MainView from './main-view';
import { useResourceStore } from './stores';

function Resource() {
  const {
    permissions,
    resourceStore: { getShowHeader },
  } = useResourceStore();

  return <Page service={permissions}>
    <CustomHeader show={getShowHeader} />
    <MainView />
  </Page>;
}

export default observer(Resource);
