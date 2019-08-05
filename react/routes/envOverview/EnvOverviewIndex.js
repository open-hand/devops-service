import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const EnvOverviewHome = asyncRouter(() => import('./envOverviewHome'), () => import('../../stores/project/envOverview'));

const EnvOverviewIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={EnvOverviewHome} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default EnvOverviewIndex;
