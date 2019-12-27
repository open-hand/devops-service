import React, { useEffect, useContext, Fragment, useState } from 'react';
import _ from 'lodash';
import { Icon, Tooltip } from 'choerodon-ui';
import { Table, Button, Tabs } from 'choerodon-ui/pro';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { Page, Permission } from '@choerodon/boot';
import { useRequestStore } from './stores';

import Loading from '../../../../components/loading';
import MouseOverWrapper from '../../../../components/MouseOverWrapper';
import ClickText from '../../../../components/click-text';
import TimePopover from '../../../../components/timePopover';
import UserInfo from '../../../../components/userInfo';

import handleMapStore from '../../main-view/store/handleMapStore';
import { useCodeManagerStore } from '../../stores';

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
    getCount: { closeCount, mergeCount, openCount, totalCount, auditCount },
    getTabKey,
    setTabKey,
  } = mergedRequestStore;


  const { appServiceDs, selectAppDs } = useCodeManagerStore();
  const appId = selectAppDs.current && selectAppDs.current.get('appServiceId');
  const appServiceData = appServiceDs.toData();

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
        service={['devops-service.devops-git.queryUrl']}
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

  function linkToNewMerge() {
    const url = `${mergedRequestStore.getUrl}/merge_requests/new`;
    window.open(url);
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
    return (
      <span style={{ textAlign: 'left' }}>!{value}</span>
    );
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

  function renderTable(params) {
    return (
      <Table dataSet={openTableDS} queryBar="none">
        <Column name="title" renderer={renderTitle} />
        <Column name="iid" renderer={renderIid} width={80} align="left" />
        <Column name="targetBranch" renderer={renderTargetBranch} />
        <Column name="createdAt" renderer={renderCreatedAt} />
        <Column name="commits" renderer={renderCommit} />
        <Column name="updatedAt" renderer={renderUpdateDate} width={100} />
      </Table>
    );
  }

  return (
    <Page
      className="c7n-region page-container c7n-merge-wrapper"
      service={[
        'devops-service.devops-git.listMergeRequest',
        'devops-service.devops-git.queryUrl',
      ]}
    >
      {appServiceDs.status !== 'ready' || !appId
        ? <Loading display />
        : <Fragment>
          <Tabs activecKey={getTabKey} onChange={tabChange} animated={false} className="c7n-merge-tabs" type="card" size="small" tabBarStyle={{ marginRight: '0' }}>
            <TabPane tab={`${formatMessage({ id: 'merge.tab1' })}(${openCount || 0})`} key="opened">
              <Table dataSet={openTableDS} queryBar="none">
                <Column name="title" renderer={renderTitle} />
                <Column name="iid" renderer={renderIid} width={80} align="left" />
                <Column name="targetBranch" renderer={renderTargetBranch} />
                <Column name="createdAt" renderer={renderCreatedAt} />
                <Column name="commits" renderer={renderCommit} />
                <Column name="updatedAt" renderer={renderUpdateDate} width={100} />
                <Column name="assignee" renderer={renderAssignee} />
              </Table>
            </TabPane>
            <TabPane tab={`${formatMessage({ id: 'merge.tab2' })}(${mergeCount || 0})`} key="merged">
              {renderTable()}
            </TabPane>
            <TabPane tab={`${formatMessage({ id: 'merge.tab3' })}(${closeCount || 0})`} key="closed">
              {renderTable()}
            </TabPane>
            <TabPane tab={`${formatMessage({ id: 'merge.tab4' })}(${totalCount || 0})`} key="all">
              <Table dataSet={openTableDS} queryBar="none">
                <Column name="title" renderer={renderTitle} />
                <Column name="iid" renderer={renderIid} align="left" width={80} />
                <Column name="targetBranch" renderer={renderTargetBranch} />
                <Column name="state" width={90} />
                <Column name="createdAt" renderer={renderCreatedAt} />
                <Column name="commits" renderer={renderCommit} />
                <Column name="updatedAt" renderer={renderUpdateDate} width={100} />
              </Table>
            </TabPane>
            {
              auditCount > 0 ? <TabPane tab={`${formatMessage({ id: 'merge.tab5' })}(${auditCount || 0})`} key="assignee">
                <Table dataSet={openTableDS} queryBar="none">
                  <Column name="title" renderer={renderTitle} />
                  <Column name="iid" renderer={renderIid} width={80} align="left" />
                  <Column name="targetBranch" renderer={renderTargetBranch} />
                  <Column name="createdAt" renderer={renderCreatedAt} />
                  <Column name="commits" renderer={renderCommit} />
                  <Column name="updatedAt" renderer={renderUpdateDate} width={100} />
                  <Column name="assignee" renderer={renderAssignee} />
                </Table>
              </TabPane> : null
            }
          </Tabs>
        </Fragment>}
    </Page>
  );
}));

export default RequestPanel;
