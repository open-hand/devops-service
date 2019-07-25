import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const ContainerHome = asyncRouter(() => import('./containerHome'), () => import('../../stores/project/container'));

const EnvironmentIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={ContainerHome} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default EnvironmentIndex;
