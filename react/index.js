import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { inject } from 'mobx-react';
import { asyncRouter, asyncLocaleProvider, nomatch } from '@choerodon/boot';
import { ModalContainer } from 'choerodon-ui/pro';

// organization
const Template = asyncRouter(() => import('./routes/template'));
const Cluster = asyncRouter(() => import('./routes/cluster'));
const CertificateManage = asyncRouter(() => import('./routes/certificate'),);
// project
const EnvPipelineIndex = asyncRouter(() => import('./routes/env-pipeline'));
const CiPipelineManageIndex = asyncRouter(() => import('./routes/ciPipelineManage'),);
const AppVersion = asyncRouter(() => import('./routes/appVersion'));
const App = asyncRouter(() => import('./routes/app-service'));
const AppStore = asyncRouter(() => import('./routes/app-store'));
const InstancesIndex = asyncRouter(() => import('./routes/deployment/main-view/contents/instance/details'));
const DeploymentApp = asyncRouter(() => import('./routes/deploymentApp'));
const NetworkConfig = asyncRouter(() => import('./routes/network'));
const Domain = asyncRouter(() => import('./routes/domain'));
const Container = asyncRouter(() => import('./routes/container'));
const AppRelease = asyncRouter(() => import('./routes/app-release'));
const Branch = asyncRouter(() => import('./routes/branch'));
const MergeRequest = asyncRouter(() => import('./routes/merge-request'));
const AppTag = asyncRouter(() => import('./routes/appTag'));
const Repository = asyncRouter(() => import('./routes/repository'));
const EnvOverview = asyncRouter(() => import('./routes/envOverview'));
const DeployOverview = asyncRouter(() => import('./routes/deploy-overview'));
const Certificate = asyncRouter(() => import('./routes/certificatePro'));
const Reports = asyncRouter(() => import('./routes/reports'));
const DevConsole = asyncRouter(() => import('./routes/devConsole'));
const ConfigMap = asyncRouter(() => import('./routes/configMap'));
const Secret = asyncRouter(() => import('./routes/secret'));
const Elements = asyncRouter(() => import('./routes/elements'));
const PipelineIndex = asyncRouter(() => import('./routes/pipeline'));
const DeploymentConfig = asyncRouter(() => import('./routes/deployment-config'));
const PipelineRecord = asyncRouter(() => import('./routes/pipeline-record'));
const CodeQuality = asyncRouter(() => import('./routes/codeQuality'));
const Notifications = asyncRouter(() => import('./routes/notifications'));
const Resource = asyncRouter(() => import('./routes/resource'));
const Deployment = asyncRouter(() => import('./routes/deployment'));
const CodeManager = asyncRouter(() => import('./routes/code-manager'));
const CertificateManageIndex = asyncRouter(() => import('./routes/certificate-manager'));


function DEVOPSIndex({ match, AppState: { currentLanguage: language } }) {
  const IntlProviderAsync = asyncLocaleProvider(language, () => import(`./locale/${language}`),);

  return (
    <IntlProviderAsync>
      <div>
        <Switch>
          <Route path={`${match.url}/env-pipeline`} component={EnvPipelineIndex} />
          <Route path={`${match.url}/env-overview`} component={EnvOverview} />
          <Route path={`${match.url}/deploy-overview`} component={DeployOverview} />
          <Route path={`${match.url}/ci-pipeline`} component={PipelineIndex} />
          {/* <Route path={`${match.url}/ci-pipeline`} component={CiPipelineManageIndex} /> */}
          <Route path={`${match.url}/template`} component={Template} />
          <Route path={`${match.url}/cluster`} component={Cluster} />
          <Route path={`${match.url}/app-version`} component={Deployment} />
          <Route path={`${match.url}/app`} component={App} />
          <Route path={`${match.url}/app-market`} component={AppStore} />
          <Route path={`${match.url}/instance`} component={InstancesIndex} />
          <Route path={`${match.url}/deployment-app`} component={DeploymentApp} />
          <Route path={`${match.url}/service`} component={NetworkConfig} />
          <Route path={`${match.url}/ingress`} component={Domain} />
          <Route path={`${match.url}/container`} component={Container} />
          <Route path={`${match.url}/app-release`} component={AppRelease} />
          <Route path={`${match.url}/branch`} component={Branch} />
          <Route path={`${match.url}/merge-request`} component={CertificateManage} />
          <Route path={`${match.url}/tag`} component={CertificateManageIndex} />
          <Route path={`${match.url}/repository`} component={Repository} />
          <Route path={`${match.url}/certificate`} component={Certificate} />
          <Route path={`${match.url}/reports`} component={Reports} />
          <Route path={`${match.url}/dev-console`} component={DevConsole} />
          <Route path={`${match.url}/config-map`} component={ConfigMap} />
          <Route path={`${match.url}/secret`} component={Secret} />
          <Route path={`${match.url}/elements`} component={Elements} />
          <Route path={`${match.url}/certificate-manage`} component={CertificateManage} />
          <Route path={`${match.url}/pipeline`} component={PipelineIndex} />
          <Route path={`${match.url}/pipeline-record`} component={PipelineRecord} />
          <Route path={`${match.url}/deployment-config`} component={DeploymentConfig} />
          {/* <Route path={`${match.url}/code-quality`} component={PipelineIndex} /> */}
          <Route path={`${match.url}/code-quality`} component={CodeManager} />
          <Route path={`${match.url}/notifications`} component={Notifications} />
          <Route path={`${match.url}/custom-resource`} component={Resource} />
          <Route path={`${match.url}/deployment`} component={Deployment} />
          <Route path="*" component={nomatch} />
        </Switch>
        <ModalContainer />
      </div>
    </IntlProviderAsync>
  );
}

export default inject('AppState')(DEVOPSIndex);
