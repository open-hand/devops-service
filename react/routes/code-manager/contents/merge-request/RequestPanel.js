import React, { useEffect, Fragment, useMemo } from 'react';
import { Icon, Tooltip } from 'choerodon-ui';
import { Table, Button, Tabs } from 'choerodon-ui/pro';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { Page, Permission } from '@choerodon/boot';
import map from 'lodash/map';
import { useRequestStore } from './stores';
import Loading from '../../../../components/loading';
import MouseOverWrapper from '../../../../components/MouseOverWrapper';
import ClickText from '../../../../components/click-text';
import TimePopover from '../../../../components/timePopover';
import UserInfo from '../../../../components/userInfo';
import handleMapStore from '../../main-view/store/handleMapStore';
import { useCodeManagerStore } from '../../stores';
import EmptyPage from '../../components/empty-page';

import './index.less';
import '../../../main.less';

const TabPane = Tabs.TabPane;
const { Column } = Table;

const RequestPanel = withRouter(observer((props) => {
  const {
    AppState: { currentMenuType: { id: projectId, id } },
    intl: { formatMessage },
    openTableDS,
    mergedRequestStore,
  } = useRequestStore();
  const {
    appServiceDs,
    selectAppDs,
  } = useCodeManagerStore();

  const {
    getCount: { closeCount, mergeCount, openCount, totalCount, auditCount },
    getTabKey,
    setTabKey,
  } = mergedRequestStore;

  const appId = selectAppDs.current && selectAppDs.current.get('appServiceId');
  const tabPaneList = useMemo(() => [
    {
      key: 'opened',
      count: openCount || 0,
    },
    {
      key: 'merged',
      count: mergeCount || 0,
    },
    {
      key: 'closed',
      count: closeCount || 0,
    },
    {
      key: 'all',
      count: totalCount || 0,
    },
  ], [closeCount, mergeCount, openCount, totalCount]);

  handleMapStore.setCodeManagerMergeRequest({
    refresh: reload,
    select: handleChange,
    getSelfToolBar,
  });

  useEffect(() => {
    mergedRequestStore.loadUser();
  }, []);

  function tabChange(key) {
    setTabKey(key);
    openTableDS.query();
  }

  function reload() {
    appId && openTableDS.query();
  }

  function handleChange() {
    appId && openTableDS.query();
  }

  function getSelfToolBar() {
    return (
      <Permission
        service={['choerodon.code.project.develop.code-management.ps.default']}
      >
        <Button
          funcType="flat"
          onClick={linkToNewMerge}
          disabled={!mergedRequestStore.getUrl}
          icon="playlist_add"
        >
          {formatMessage({ id: 'merge.createMerge' })}
        </Button>
      </Permission>
    );
  }

  async function linkToNewMerge() {
    try {
      const res = await mergedRequestStore.checkMerge(projectId, appId);
      if (res) {
        const url = `${mergedRequestStore.getUrl}/merge_requests/new`;
        window.open(url);
      }
    } catch (e) {
      // return;
    }
  }

  function linkToMerge(iid) {
    const url = `${mergedRequestStore.getUrl}/merge_requests/${iid}`;
    window.open(url);
  }

  function renderTitle({ value, record }) {
    return (
      <MouseOverWrapper text={value} width={0.25}>
        <ClickText value={value} clickAble onClick={linkToMerge} record={record.get('iid')} />
      </MouseOverWrapper>
    );
  }

  function renderIid({ value }) {
    return <span>!{value}</span>;
  }

  function renderTargetBranch({ value, record }) {
    return (
      <div
        className="c7n-merge-branches"
        style={{
          display: 'flex',
          alignItems: 'center',
        }}
      >
        <Icon type="branch" />
        <MouseOverWrapper text={record.get('sourceBranch')} width={0.1}>{record.get('sourceBranch')}</MouseOverWrapper>
        <Icon type="keyboard_backspace" className="c7n-merge-right" />
        <Icon type="branch" />
        <span>{value}</span>
      </div>
    );
  }

  function renderCreatedAt({ value, record }) {
    const author = record.get('author');
    return (
      <div className="c7ncd-merge-create-info">
        {author ? (
          <Tooltip title={`${author.name}${author.username ? `(${author.username})` : ''}`}>
            {author.avatarUrl
              ? <img className="c7n-merge-avatar" src={author.avatarUrl} alt="avatar" />
              : <span className="apptag-commit apptag-commit-avatar">{author.name.toString().substr(0, 1)}</span>}
          </Tooltip>
        ) : <span className="apptag-commit apptag-commit-avatar">?</span>}
        <TimePopover content={value} />
      </div>
    );
  }

  function renderCommit({ value, record }) {
    return (
      <div>
        {value && value.length ? `${value.length} commits` : '0 commit'}
      </div>
    );
  }

  function renderUpdateDate({ value }) {
    return <TimePopover content={value} />;
  }

  function renderAssignee({ value }) {
    return value ? (
      <UserInfo name={value.name || ''} id={value.username} avatar={value.avatarUrl} />
    ) : formatMessage({ id: 'merge.noAssignee' });
  }

  function renderTable(tabPaneKey) {
    return (
      <Table dataSet={openTableDS} queryBar="none">
        <Column name="title" renderer={renderTitle} />
        <Column name="iid" renderer={renderIid} width={80} align="left" />
        <Column name="targetBranch" renderer={renderTargetBranch} />
        {tabPaneKey === 'all' && <Column name="state" width={90} />}
        <Column name="createdAt" renderer={renderCreatedAt} />
        <Column name="commits" renderer={renderCommit} />
        <Column name="updatedAt" renderer={renderUpdateDate} width={100} />
        {(tabPaneKey === 'opened' || tabPaneKey === 'assignee') && (
          <Column name="assignee" renderer={renderAssignee} />
        )}
      </Table>
    );
  }

  return (
    <Page
      className="c7n-region page-container c7n-merge-wrapper"
      service={[]}
    >
      {appServiceDs.status !== 'ready' || !appId
        ? <Loading display />
        : (!mergedRequestStore.getIsEmpty ? (<Fragment>
          <Tabs
            activecKey={getTabKey}
            onChange={tabChange}
            animated={false}
            className="c7n-merge-tabs"
            type="card"
            size="small"
            tabBarStyle={{ marginRight: '0' }}
          >
            {map(tabPaneList, ({ key, count }) => (
              <TabPane
                tab={`${formatMessage({ id: `merge.tab.${key}` })}(${count})`}
                key={key}
              >
                {renderTable(key)}
              </TabPane>
            ))}
            {auditCount > 0 && (
              <TabPane
                tab={`${formatMessage({ id: 'merge.tab.assignee' })}(${auditCount || 0})`}
                key="assignee"
              >
                {renderTable('assignee')}
              </TabPane>
            )}
          </Tabs>
        </Fragment>) : (
          <EmptyPage
            title={formatMessage({ id: 'empty.title.prohibited' })}
            describe={formatMessage({ id: 'empty.title.code' })}
            btnText={formatMessage({ id: 'empty.link.code' })}
            pathname="/rducm/code-lib-management/apply"
          />
        ))}
    </Page>
  );
}));

export default RequestPanel;
