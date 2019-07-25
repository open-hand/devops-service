import React from 'react/index';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const pipelineRecord = asyncRouter(
  () => import('./record-table'),
  () => import('./record-table/stores'),
);
const PipelineDetail = asyncRouter(() => import('./pipeline-detail'), () => import('../pipeline/pipeline-table/stores'));

const pipelineRecordIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={pipelineRecord} />
    <Route path={`${match.url}/detail/:pId/:rId`} component={PipelineDetail} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default pipelineRecordIndex;

