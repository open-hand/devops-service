import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const ConfigMapHome = asyncRouter(() => import('./configMapHome'), () => import('../../stores/project/configMap'));

const ConfigMapIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={ConfigMapHome} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default ConfigMapIndex;
