import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const TemplateHome = asyncRouter(() => import('./template-home'), () => import('./stores'));

const TemplateIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={TemplateHome} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default TemplateIndex;
