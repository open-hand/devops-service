import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const Resource = asyncRouter(
  () => import('./resourceHome'),
  () => import('../../../stores/project/resource'),
);

const ResourceIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={Resource} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default ResourceIndex;
