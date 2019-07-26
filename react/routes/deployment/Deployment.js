import React, { useContext } from 'react';
import { inject } from 'mobx-react';
import { Page, Header } from '@choerodon/boot';
import { FormattedMessage } from 'react-intl';
import MainView from './main-view';
import Store from './stores';

const Deployment = () => {
  const { permissions } = useContext(Store);

  return <Page service={permissions}>
    <Header title={<FormattedMessage id="deployment.header" />} />
    <MainView />
  </Page>;
};

export default inject('AppState')(Deployment);
