import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const ClusterHome = asyncRouter(() => import('./home'), () => import('./stores'));
const NodeDetail= asyncRouter(() => import('./node-detail'), () => import('./stores'));

const ClusterIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={ClusterHome} />
    <Route exact path={`${match.url}/:clusterId/node`} component={NodeDetail} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default ClusterIndex;
