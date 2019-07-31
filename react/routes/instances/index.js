import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const Instance = asyncRouter(
  () => import('./instances-home'),
  () => import('./instances-home/stores/InstancesStore.js'),
);
const InstancesDetail = asyncRouter(
  () => import('./instances-detail'),
  () => import('./instances-detail/stores/InstanceDetailStore'),
);

const InstancesIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={Instance} />
    <Route
      exact
      path={`${match.url}/:id/:status/:instanceName/detail`}
      component={InstancesDetail}
    />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default InstancesIndex;
