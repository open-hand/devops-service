import React, { useContext, useEffect } from 'react';
import { Page, Header } from '@choerodon/boot';
import { FormattedMessage } from 'react-intl';
import MainView from './main-view';
import Store from './stores';

export default function Deployment() {
  const { permissions } = useContext(Store);

  return <Page service={permissions}>
    <Header title={<FormattedMessage id="deployment.header" />} />
    <MainView />
  </Page>;
}
