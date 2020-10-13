import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { inject } from 'mobx-react';
import { asyncRouter, asyncLocaleProvider, nomatch } from '@choerodon/boot';
import { ModalContainer } from 'choerodon-ui/pro';

import './index.less';

const AppService = asyncRouter(() => import('./routes/app-service'));
const Code = asyncRouter(() => import('./routes/code-manager'));
const Resource = asyncRouter(() => import('./routes/resource'));
const Deployment = asyncRouter(() => import('./routes/deployment'));
const Pipeline = asyncRouter(() => import('./routes/pipeline'));
const Certificate = asyncRouter(() => import('./routes/certificate'));
const Cluster = asyncRouter(() => import('./routes/cluster'));
const Environment = asyncRouter(() => import('./routes/environment'));
const Reports = asyncRouter(() => import('./routes/reports'));
const Repository = asyncRouter(() => import('./routes/repository'));
const ProRepository = asyncRouter(() => import('./routes/pro-repository'));
const PVManager = asyncRouter(() => import('./routes/pv-manager'));
const PipelineManage = asyncRouter(() => import('./routes/pipeline-manage'));
const HostConfig = asyncRouter(() => import('./routes/host-config'));

function DEVOPSIndex({ match, AppState: { currentLanguage: language } }) {
  const IntlProviderAsync = asyncLocaleProvider(language, () => import(`./locale/${language}`));
  return (
    <IntlProviderAsync>
      <div className="c7ncd-root">
        <Switch>
          <Route path={`${match.url}/app-service`} component={AppService} />
          <Route path={`${match.url}/code-management`} component={Code} />
          <Route path={`${match.url}/resource`} component={Resource} />
          <Route path={`${match.url}/deployment-operation`} component={Deployment} />
          <Route path={`${match.url}/pipeline-manage`} component={PipelineManage} />
          <Route path={`${match.url}/pipeline`} component={Pipeline} />
          <Route path={`${match.url}/environment`} component={Environment} />
          <Route path={`${match.url}/cert-management`} component={Certificate} />
          <Route path={`${match.url}/cluster-management`} component={Cluster} />
          <Route path={`${match.url}/repository`} component={Repository} />
          <Route path={`${match.url}/project-repository`} component={ProRepository} />
          <Route path={`${match.url}/reports`} component={Reports} />
          <Route path={`${match.url}/pv-management`} component={PVManager} />
          <Route path={`${match.url}/host-config`} component={HostConfig} />
          <Route path="*" component={nomatch} />
        </Switch>
        <ModalContainer />
      </div>
    </IntlProviderAsync>
  );
}

export default inject('AppState')(DEVOPSIndex);
