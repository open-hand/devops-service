import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const AppReleaseHome = asyncRouter(() => import('./release-home'), () => import('./release-home/stores'));
const AppCreateDetail = asyncRouter(() => import('./create-edit/release-edit'), () => import('./create-edit/stores'));
const AddAppRelease = asyncRouter(() => import('./create-edit/release-add'), () => import('./create-edit/stores'));
const EditVersions = asyncRouter(() => import('./versions-edit'), () => import('./versions-edit/stores'));

const EnvironmentIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={AppReleaseHome} />
    <Route exact path={`${match.url}/:key`} component={AppReleaseHome} />
    <Route exact path={`${match.url}/edit/:id`} component={AppCreateDetail} />
    <Route exact path={`${match.url}/add/:appId`} component={AddAppRelease} />
    <Route exact path={`${match.url}/app/:name/edit-version/:id`} component={EditVersions} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default EnvironmentIndex;
