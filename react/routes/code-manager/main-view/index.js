import React from 'react';
import { observer, inject } from 'mobx-react';
import { PageWrap, PageTab } from '@choerodon/master';
import CodeQuality from './code-quality';
import CodeManagerBranch from './branch';
import CodeManagerMergeRequest from './merge-request';
import CodeManagerAppTag from './app-tag';
import CodeManagerCiPipelineManage from './ci-pipeline-manage';


import './index.less'; 


const MainView = observer(() => <PageWrap noHeader={[]}>
  <PageTab title="分支" tabKey="key1" component={CodeManagerBranch} />
  <PageTab title="合并请求" tabKey="key2" component={CodeManagerMergeRequest} />
  <PageTab title="持续集成" tabKey="key3" component={CodeManagerCiPipelineManage} />
  <PageTab title="标记" tabKey="key4" component={CodeManagerAppTag} />
  <PageTab title="代码质量" tabKey="key5" component={CodeQuality} />
  
</PageWrap>);


export default MainView;
