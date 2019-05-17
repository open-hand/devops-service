import React from 'react';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const pipelineRecord = asyncRouter(
  () => import('./pipelineRecordHome'),
  () => import('../../../stores/project/pipelineRecord'),
);
const PipelineDetail = asyncRouter(() => import('./pipelineDetail'), () => import('../../../stores/project/pipeline'));

const pipelineRecordIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={pipelineRecord} />
    <Route path={`${match.url}/detail/:pId/:rId`} component={PipelineDetail} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default pipelineRecordIndex;

