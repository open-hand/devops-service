import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const DeploymentConfig = asyncRouter(
  () => import('./deploymentConfigHome'),
  () => import('../../../stores/project/deploymentConfig')
);

const SecretIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={DeploymentConfig} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default SecretIndex;

