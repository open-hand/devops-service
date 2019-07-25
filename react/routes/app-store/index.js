import React from 'react/index';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const AppStoreHome = asyncRouter(() => import('./store-home'), () => import('./stores'));
const AppDetail = asyncRouter(() => import('./app-detail'), () => import('./stores'));
const ImportChart = asyncRouter(() => import('./import-chart'), () => import('./stores'));
const ExportChart = asyncRouter(() => import('./export-chart'), () => import('./export-chart/stores'));

const AppStoreIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={AppStoreHome} />
    <Route exact path={`${match.url}/:id/app`} component={AppDetail} />
    <Route exact path={`${match.url}/import`} component={ImportChart} />
    <Route exact path={`${match.url}/export`} component={ExportChart} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default AppStoreIndex;
