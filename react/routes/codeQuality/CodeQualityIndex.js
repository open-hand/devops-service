import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const CodeQuality = asyncRouter(
  () => import('./codeQualityHome'),
  () => import('../../stores/project/codeQuality')
);

const codeQualityIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={CodeQuality} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default codeQualityIndex;

