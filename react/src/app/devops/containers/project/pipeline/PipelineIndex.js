import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const PipelineHome = asyncRouter(() => import('./pipelineHome'), () => import('../../../stores/project/pipeline'));
const PipelineCreate = asyncRouter(() => import('./pipelineCreate'), () => import('../../../stores/project/pipeline/PipelineCreateStore'));
const PipelineEdit = asyncRouter(() => import('./pipelineEdit'), () => import('../../../stores/project/pipeline/PipelineCreateStore'));

const PipelineIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={PipelineHome} />
    <Route path={`${match.url}/create`} component={PipelineCreate} />
    <Route path={`${match.url}/edit/:id`} component={PipelineEdit} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default PipelineIndex;
