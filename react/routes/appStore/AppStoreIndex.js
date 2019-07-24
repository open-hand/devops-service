import React from 'react/index';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const AppStoreHome = asyncRouter(() => import('./appStoreHome'), () => import('../../stores/project/appStore'));
const AppDetail = asyncRouter(() => import('./appDetail'), () => import('../../stores/project/appStore'));
const ImportChart = asyncRouter(() => import('./importChart'), () => import('../../stores/project/appStore'));
const ExportChart = asyncRouter(() => import('./exportChart'), () => import('../../stores/project/appStore/exportChart'));

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
