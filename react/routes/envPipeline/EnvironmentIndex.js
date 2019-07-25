import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const Environment = asyncRouter(() => import('./Environment'), () => import('../../stores/project/envPipeline'));

const EnvironmentIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={Environment} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default EnvironmentIndex;
