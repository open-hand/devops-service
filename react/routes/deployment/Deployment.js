import React, { useEffect, useContext } from 'react';
import { inject } from 'mobx-react';
import { Page, Header } from '@choerodon/boot';
import { FormattedMessage } from 'react-intl';
import MainView from './main-view';
import Store from './stores';

const Deployment = ({ AppState: { currentMenuType } }) => {
  const { store } = useContext(Store);

  useEffect(() => {
    store.loadNavData(currentMenuType.id);
  }, [currentMenuType.id]);

  return <Page
    service={[
      'devops-service.application-instance.pageByOptions',
    ]}
  >
    <Header title={<FormattedMessage id="deployment.header" />} />
    <MainView />
  </Page>;
};

export default inject('AppState')(Deployment);
