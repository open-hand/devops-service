import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const Secret = asyncRouter(() => import('./secretHome'), () => import('../../../stores/project/secret'));

const SecretIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={Secret} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default SecretIndex;
