import React from 'react';
import { Switch, Route } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const InstanceView = asyncRouter(() => import('./instance-view'));
const ResourceView = asyncRouter(() => import('./resource-view'));

export default function ViewIndex({ match }) {
  return (
    <Switch>
      <Route exact path={`${match.url}/instance-view`} component={InstanceView} />
      <Route exact path={`${match.url}/resource-view`} component={ResourceView} />
      <Route path="*" component={nomatch} />
    </Switch>
  );
}
