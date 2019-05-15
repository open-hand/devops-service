import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const AppVersionHome = asyncRouter(() => import('./applicationHome'), () => import('../../../stores/project/applicationVersion'));

const EnvironmentIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={AppVersionHome} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default EnvironmentIndex;
