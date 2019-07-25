import React from 'react/index';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const NetworkHome = asyncRouter(() => import('./network-table'), () => import('./stores'));

const networkConfigIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={NetworkHome} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default networkConfigIndex;
