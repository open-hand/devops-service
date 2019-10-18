import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';
import { StoreProvider } from './stores';

const AppList = asyncRouter(() => import('./app-list'));
const ServiceDetail = asyncRouter(() => import('./service-detail'));

export default ({ match }) => (
  <StoreProvider>
    <Switch>
      <Route exact path={match.url} component={AppList} />
      <Route exact path={`${match.url}/detail/:id`} component={ServiceDetail} />
      <Route path="*" component={nomatch} />
    </Switch>
  </StoreProvider>
);
