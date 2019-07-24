import React from 'react/index';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const DomainHome = asyncRouter(() => import('./domainHome'), () => import('../../stores/project/domain'));

const domainIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={DomainHome} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default domainIndex;
