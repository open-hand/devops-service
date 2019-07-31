import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';

import { asyncRouter, nomatch } from '@choerodon/boot';

const App = asyncRouter(() => import('./app-table'), () => import('./stores'));
const AppImport = asyncRouter(() => import('./app-import'), () => import('./stores'));

const EnvironmentIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={App} />
    <Route exact path={`${match.url}/import`} component={AppImport} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default EnvironmentIndex;
