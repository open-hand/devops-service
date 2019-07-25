import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const Instance = asyncRouter(
  () => import('./Instances'),
  () => import('../../stores/project/instances/InstancesStore.js'),
);
const InstancesDetail = asyncRouter(
  () => import('./instancesDetail'),
  () => import('../../stores/project/instances/InstanceDetailStore'),
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
