import React from 'react';
import { observer } from 'mobx-react-lite';
import { Page } from '@choerodon/boot';
import CustomHeader from '../../components/custom-header';
import MainView from './main-view';
import { useResourceStore } from './stores';

function Resource() {
  const {
    permissions,
    resourceStore: { getShowHeader },
  } = useResourceStore();

  return <Page service={['choerodon.code.project.deploy.app-deployment.resource.ps.default']}>
    <CustomHeader show={getShowHeader} />
    <MainView />
  </Page>;
}

export default observer(Resource);
