import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const ClusterHome = asyncRouter(() => import('./Home'), () => import('../../../stores/organization/cluster'));
const NodeDetail= asyncRouter(() => import('./NodeDetail'), () => import('../../../stores/organization/cluster'));

const ClusterIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={ClusterHome} />
    <Route exact path={`${match.url}/:clusterId/node`} component={NodeDetail} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default ClusterIndex;
