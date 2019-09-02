import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/master';

const REPORTSHOME = asyncRouter(() => import('./Home'), () => import('./stores'));
const SUBMISSION = asyncRouter(() => import('./Submission'), () => import('./stores'));
const CodeQuality = asyncRouter(() => import('./CodeQuality'), () => import('./stores'));
const BUILDNUMBER = asyncRouter(() => import('./BuildNumber'), () => import('./stores'));
const BUILDDURATION = asyncRouter(() => import('./BuildDuration'), () => import('./stores'));
const DeployTimes = asyncRouter(() => import('./DeployTimes'), () => import('./stores'));
const DeployDuration = asyncRouter(() => import('./DeployDuration'), () => import('./stores'));

const ReportsIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={REPORTSHOME} />
    <Route exact path={`${match.url}/submission`} component={SUBMISSION} />
    <Route exact path={`${match.url}/code-quality`} component={CodeQuality} /> 
    <Route exact path={`${match.url}/build-number`} component={BUILDNUMBER} />
    <Route exact path={`${match.url}/build-duration`} component={BUILDDURATION} />
    <Route exact path={`${match.url}/deploy-times`} component={DeployTimes} />
    <Route exact path={`${match.url}/deploy-duration`} component={DeployDuration} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default ReportsIndex;
