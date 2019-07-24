import React from 'react';
import { Switch, Route } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const Env = asyncRouter(() => import('./environment'));

export default function InstanceViewIndex({ match }) {
  return (
    <Switch>
      <Route exact path={`${match.url}/environment/:id`} component={Env} />
      <Route path="*" component={nomatch} />
    </Switch>
  );
}
