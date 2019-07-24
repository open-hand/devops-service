import React from 'react';
import { Page, Header } from '@choerodon/boot';
import { FormattedMessage } from 'react-intl';

const Deployment = (() => <Page
  service={[
    'devops-service.application-instance.pageByOptions',
  ]}
>
  <Header title={<FormattedMessage id="deployment.header" />} />
</Page>);

export default Deployment;
