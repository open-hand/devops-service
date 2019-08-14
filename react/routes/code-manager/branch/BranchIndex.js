/**
 *create by mading on 2018/3/28
 */
import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';

import { asyncRouter, nomatch } from '@choerodon/boot';

const BranchHome = asyncRouter(() => import('./branchHome'), () => import('../../stores/project/branchManage'));

const EnvironmentIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={BranchHome} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default EnvironmentIndex;
