import React, { Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { PageWrap, PageTab, Page } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import map from 'lodash/map';
import { useCodeManagerStore } from '../stores';
import EmptyShown, { EmptyLoading } from './empty';
import CodeQuality from './code-quality';
import CodeManagerBranch from './branch';
import CodeManagerMergeRequest from './merge-request';
import CodeManagerAppTag from './app-tag';
import CodeManagerCiPipelineManage from './ci-pipeline-manage';
import Tips from '../../../components/new-tips';

import './index.less';

const MainView = injectIntl(observer((props) => {
  const { intl: { formatMessage } } = props;
  const {
    permissions,
    codeManagerStore,
  } = useCodeManagerStore();
  const { getLoading, getHasApp } = codeManagerStore;

  function getContent() {
    if (getLoading) return <EmptyLoading formatMessage={formatMessage} />;

    const titleData = ['master', 'feature', 'bugfix', 'release', 'hotfix', 'custom'];
    const popoverContent = map(titleData, item => (<div className="c7n-branch-block" key={item}>
      <span className={`branch-popover-span span-${item}`} />
      <div className="branch-popover-content">
        <p className="branch-popover-p">
          <FormattedMessage id={`branch.${item}`} />
        </p>
        <p>
          <FormattedMessage id={`branch.${item}Des`} />
        </p>
      </div>
    </div>));

    return getHasApp ? <PageWrap noHeader={[]} cache>
      <PageTab
        title={<Tips
          helpText={popoverContent}
          title={formatMessage({ id: 'code-management.branch' })}
          popoverClassName="branch-popover"
        />}
        tabKey="key1"
        component={CodeManagerBranch}
        alwaysShow
      />
      <PageTab
        title={formatMessage({ id: 'code-management.merge-request' })}
        tabKey="key2"
        component={CodeManagerMergeRequest}
        alwaysShow
      />
      <PageTab
        title={formatMessage({ id: 'code-management.ci-pipeline' })}
        tabKey="key3"
        component={CodeManagerCiPipelineManage}
        alwaysShow
      />
      <PageTab
        title={formatMessage({ id: 'code-management.app-tag' })}
        tabKey="key4"
        component={CodeManagerAppTag}
        alwaysShow
      />
      <PageTab
        title={formatMessage({ id: 'code-management.code-quality' })}
        tabKey="key5"
        component={CodeQuality}
        alwaysShow
      />
    </PageWrap> : <EmptyShown />;
  }
  return (<Page service={permissions}>
    <div className="c7n-code-managerment-tab-list">
      {getContent()}
    </div>
  </Page>);
}));

export default MainView;
