import React from 'react';
import { Page, Header } from '@choerodon/boot';
import { FormattedMessage } from 'react-intl';
import MainView from './main-view';

const Deployment = (() => <Page
  service={[
    'devops-service.application-instance.pageByOptions',
  ]}
>
  <Header title={<FormattedMessage id="deployment.header" />} />
  <MainView />
</Page>);

export default Deployment;
