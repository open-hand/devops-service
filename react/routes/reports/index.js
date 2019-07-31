import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const REPORTSHOME = asyncRouter(() => import('./report-list'), () => import('./stores'));
const SUBMISSION = asyncRouter(() => import('./submission'), () => import('./stores'));
const DeployDuration = asyncRouter(() => import('./deploy-duration'), () => import('./stores'));
const DeployTimes = asyncRouter(() => import('./deploy-times'), () => import('./stores'));
const BUILDNUMBER = asyncRouter(() => import('./build-number'), () => import('./stores'));
const BUILDDURATION = asyncRouter(() => import('./build-duration'), () => import('./stores'));
const CodeQuality = asyncRouter(() => import('./code-quality'), () => import('./stores'));

const ReportsIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={REPORTSHOME} />
    <Route exact path={`${match.url}/submission`} component={SUBMISSION} />
    <Route exact path={`${match.url}/deploy-duration`} component={DeployDuration} />
    <Route exact path={`${match.url}/deploy-times`} component={DeployTimes} />
    <Route exact path={`${match.url}/build-number`} component={BUILDNUMBER} />
    <Route exact path={`${match.url}/build-duration`} component={BUILDDURATION} />
    <Route exact path={`${match.url}/code-quality`} component={CodeQuality} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default ReportsIndex;
