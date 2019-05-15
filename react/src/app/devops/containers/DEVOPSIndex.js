import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { inject } from 'mobx-react';
import {
  asyncRouter,
  nomatch,
  asyncLocaleProvider,
} from '@choerodon/boot';
import InstancesStore from '../stores/project/instances/InstancesStore';

// organization
const Template = asyncRouter(() => import('./organization/template'));
const Cluster = asyncRouter(() => import('./organization/cluster'));
const CertificateManage = asyncRouter(() =>
  import('./organization/certificate'),
);
// project
const EnvPipelineIndex = asyncRouter(() => import('./project/envPipeline'));
const CiPipelineManageIndex = asyncRouter(() =>
  import('./project/ciPipelineManage'),
);
const AppVersion = asyncRouter(() => import('./project/appVersion'));
const App = asyncRouter(() => import('./project/app'));
const AppStore = asyncRouter(() => import('./project/appStore'));
const InstancesIndex = asyncRouter(() => import('./project/instances'));
const DeploymentApp = asyncRouter(() => import('./project/deploymentApp'));
const NetworkConfig = asyncRouter(() => import('./project/networkConfig'));
const Domain = asyncRouter(() => import('./project/domain'));
const Container = asyncRouter(() => import('./project/container'));
const AppRelease = asyncRouter(() => import('./project/appRelease'));
const Branch = asyncRouter(() => import('./project/branch'));
const MergeRequest = asyncRouter(() => import('./project/mergeRequest'));
const AppTag = asyncRouter(() => import('./project/appTag'));
const Repository = asyncRouter(() => import('./project/repository'));
const EnvOverview = asyncRouter(() => import('./project/envOverview'));
const DeployOverview = asyncRouter(() => import('./project/deployOverview'));
const Certificate = asyncRouter(() => import('./project/certificate'));
const Reports = asyncRouter(() => import('./project/reports'));
const DevConsole = asyncRouter(() => import('./project/devConsole'));
const ConfigMap = asyncRouter(() => import('./project/configMap'));
const Secret = asyncRouter(() => import('./project/secret'));
const Elements = asyncRouter(() => import('./project/elements'));
const PipelineIndex = asyncRouter(() => import('./project/pipeline'));
const DeploymentConfig = asyncRouter(() => import('./project/deploymentConfig'));
const PipelineRecord = asyncRouter(() => import('./project/pipelineRecord'));
const CodeQuality = asyncRouter(() => import('./project/codeQuality'));

@inject('AppState')
class DEVOPSIndex extends React.Component {
  render() {
    const { match, AppState } = this.props;
    const language = AppState.currentLanguage;
    const IntlProviderAsync = asyncLocaleProvider(language, () =>
      import(`../locale/${language}`),
    );

    /**
     * 从实例详情页面跳转到实例页面或容器不重新加载
     * 从详情页面跳到其他页面需要重新加载
     * isCache 设置一个标志位，true表示不重新加载，否则重新加载
     */
    const isInstancePage = window.location.href.includes('instance') || window.location.href.includes('container');
    if (InstancesStore.getIsCache && !isInstancePage) {
      InstancesStore.setIsCache(false);
      InstancesStore.setAppId(null);
      InstancesStore.setAppNameByEnv([]);
      InstancesStore.clearIst();
      InstancesStore.setIstTableFilter(null);
      InstancesStore.setIstPage(null);
    }

    return (
      <IntlProviderAsync>
        <Switch>
          <Route path={`${match.url}/env-pipeline`} component={EnvPipelineIndex} />
          <Route path={`${match.url}/env-overview`} component={EnvOverview} />
          <Route path={`${match.url}/deploy-overview`} component={DeployOverview} />
          <Route path={`${match.url}/ci-pipeline`} component={CiPipelineManageIndex} />
          <Route path={`${match.url}/template`} component={Template} />
          <Route path={`${match.url}/cluster`} component={Cluster} />
          <Route path={`${match.url}/app-version`} component={AppVersion} />
          <Route path={`${match.url}/app`} component={App} />
          <Route path={`${match.url}/app-market`} component={AppStore} />
          <Route path={`${match.url}/instance`} component={InstancesIndex} />
          <Route path={`${match.url}/deployment-app`} component={DeploymentApp} />
          <Route path={`${match.url}/service`} component={NetworkConfig} />
          <Route path={`${match.url}/ingress`} component={Domain} />
          <Route path={`${match.url}/container`} component={Container} />
          <Route path={`${match.url}/app-release`} component={AppRelease} />
          <Route path={`${match.url}/branch`} component={Branch} />
          <Route path={`${match.url}/merge-request`} component={MergeRequest} />
          <Route path={`${match.url}/tag`} component={AppTag} />
          <Route path={`${match.url}/repository`} component={Repository} />
          <Route path={`${match.url}/certificate`} component={Certificate} />
          <Route path={`${match.url}/reports`} component={Reports} />
          <Route path={`${match.url}/dev-console`} component={DevConsole} />
          <Route path={`${match.url}/config-map`} component={ConfigMap} />
          <Route path={`${match.url}/secret`} component={Secret} />
          <Route path={`${match.url}/elements`} component={Elements} />
          <Route path={`${match.url}/certificate-manage`} component={CertificateManage} />
          <Route path={`${match.url}/pipeline`} component={PipelineIndex}/>
          <Route path={`${match.url}/pipeline-record`} component={PipelineRecord}/>
          <Route path={`${match.url}/deployment-config`} component={DeploymentConfig} />
          <Route path={`${match.url}/code-quality`} component={CodeQuality} />
          <Route path="*" component={nomatch} />
        </Switch>
      </IntlProviderAsync>
    );
  }
}

export default DEVOPSIndex;
