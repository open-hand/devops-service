import React from 'react';
import { observer, inject } from 'mobx-react';
import { PageWrap, PageTab } from '@choerodon/master';
import { injectIntl } from 'react-intl';
import CodeQuality from './code-quality';
import CodeManagerBranch from './branch';
import CodeManagerMergeRequest from './merge-request';
import CodeManagerAppTag from './app-tag';
import CodeManagerCiPipelineManage from './ci-pipeline-manage';


import './index.less'; 


const MainView = injectIntl(observer((props) => { 
  const { intl: { formatMessage } } = props;
  return (
    <div className="c7n-code-managerment-tab-list"> <PageWrap noHeader={[]}>
      <PageTab title={formatMessage({ id: 'code-management.branch' })} tabKey="key1" component={CodeManagerBranch} alawaysShow />
      <PageTab title={formatMessage({ id: 'code-management.merge-request' })} tabKey="key2" component={CodeManagerMergeRequest} alawaysShow />
      <PageTab title={formatMessage({ id: 'code-management.ci-pipeline' })} tabKey="key3" component={CodeManagerCiPipelineManage} alawaysShow />
      <PageTab title={formatMessage({ id: 'code-management.app-tag' })} tabKey="key4" component={CodeManagerAppTag} alawaysShow />
      <PageTab title={formatMessage({ id: 'code-management.code-quality' })} tabKey="key5" component={CodeQuality} alawaysShow />
    </PageWrap></div>);
}));


export default MainView;
