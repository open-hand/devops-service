import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

import { StoreProvider } from './stores';

const REPORTSHOME = asyncRouter(() => import('./Home'));
const SUBMISSION = asyncRouter(() => import('./Submission'));
const CodeQuality = asyncRouter(() => import('./CodeQuality'));
const BUILDNUMBER = asyncRouter(() => import('./BuildNumber'));
const BUILDDURATION = asyncRouter(() => import('./BuildDuration'));
const DeployTimes = asyncRouter(() => import('./DeployTimes'));
const DeployDuration = asyncRouter(() => import('./DeployDuration'));

const ReportsIndex = (props) => {
  const { match } = props;
  return (
    <StoreProvider {...props}>
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
    </StoreProvider>
  );
};

export default ReportsIndex;
