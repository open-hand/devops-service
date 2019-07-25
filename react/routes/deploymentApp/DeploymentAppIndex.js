import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const DeploymentApp = asyncRouter(() => import('./deploymentAppHome'), () => import('../../stores/project/deployApp'));

const DeploymentAppIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={DeploymentApp} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default DeploymentAppIndex;
