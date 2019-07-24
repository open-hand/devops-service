import React from 'react/index';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const REPORTSHOME = asyncRouter(() => import('./Home'), () => import('../../stores/project/reports'));
const SUBMISSION = asyncRouter(() => import('./Submission'), () => import('../../stores/project/reports'));
const DeployDuration = asyncRouter(() => import('./DeployDuration'), () => import('../../stores/project/reports'));
const DeployTimes = asyncRouter(() => import('./DeployTimes'), () => import('../../stores/project/reports'));
const BUILDNUMBER = asyncRouter(() => import('./BuildNumber'), () => import('../../stores/project/reports'));
const BUILDDURATION = asyncRouter(() => import('./BuildDuration'), () => import('../../stores/project/reports'));
const CodeQuality = asyncRouter(() => import('./CodeQuality'), () => import('../../stores/project/reports'));

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
