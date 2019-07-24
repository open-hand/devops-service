import React from 'react/index';
import { Route, Switch } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const ElementsHome = asyncRouter(() => import('./elementsHome'), () => import('../../stores/project/elements'));

const elementsIndex = ({ match }) => (<Switch>
  <Route exact path={match.url} component={ElementsHome} />
  <Route path="*" component={nomatch} />
</Switch>);

export default elementsIndex;
