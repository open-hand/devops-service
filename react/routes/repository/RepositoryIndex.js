import React from 'react/index';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const RepositoryHome = asyncRouter(() => import('./repositoryHome'), () => import('../../stores/project/repository'));

const RepositoryIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={RepositoryHome} />
    <Route path={'*'} component={nomatch} />
  </Switch>
);

export default RepositoryIndex;
