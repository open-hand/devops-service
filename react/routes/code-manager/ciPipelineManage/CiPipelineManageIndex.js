import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/master';

const CiPipelineHome = asyncRouter(() => import('./ciPipelineHome/CiPipelineHome'));

const CiPipelineIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={CiPipelineHome} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default CiPipelineIndex;
