import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const AppTagHome = asyncRouter(() => import('./appTagHome'), () => import('../../../stores/project/appTag'));

const RepositoryIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={AppTagHome} />
    <Route path={'*'} component={nomatch} />
  </Switch>
);

export default RepositoryIndex;
