import React, { useMemo, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Output, Spin, Icon } from 'choerodon-ui/pro';
import { useRecordDetailStore } from './stores';
import StatusTag from '../PipelineFlow/components/StatusTag';
import UserInfo from '../../../../components/userInfo';
import getDuration from '../../../../utils/getDuration';

import './index.less';

export default observer(() => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { projectId } },
    intlPrefix,
    prefixCls,
    detailDs,
    store,
  } = useRecordDetailStore();

  const record = useMemo(() => detailDs.current, [detailDs.current]);

  function renderUser() {
    const { createUserUrl, createUserName } = record.get('ciCdPipelineVO') || {};
    return <UserInfo name={createUserName || ''} avatar={createUserUrl} />;
  }

  function renderDuration() {
    const { time } = record.get('ciCdPipelineVO') || {};
    return getDuration({ value: time, unit: 's' });
  }

  function renderPipelineName() {
    const { name } = record.get('ciCdPipelineVO') || {};
    return name;
  }

  function appServiceName() {
    const { appServiceName: name } = record.get('ciCdPipelineVO') || {};
    return name;
  }

  async function linkToGitlab(url) {
    try {
      const { appServiceId } = record.get('ciCdPipelineVO') || {};
      await store.checkLinkToGitlab(projectId, appServiceId);
      window.open(url);
    } catch (e) {
      // return;
    }
  }

  function renderCreateDate() {
    const { latestExecuteDate } = record.get('ciCdPipelineVO') || {};
    return latestExecuteDate;
  }
  
  function renderCommit({ value }) {
    const { commitContent, commitSha, commitUrl, ref, userHeadUrl, userName, gitlabProjectUrl } = value || {};
    return (
      <div className={`${prefixCls}-commit`}>
        <div className={`${prefixCls}-commit-title`}>
          <Icon type="branch" className={`${prefixCls}-commit-title-branch`} />
          <span
            onClick={() => linkToGitlab(`${gitlabProjectUrl}/commits/${ref}`)}
            className={`${prefixCls}-commit-title-ref`}
          >
            <span>{ref}</span>
          </span>
          <Icon type="point" className={`${prefixCls}-commit-title-point`} />
          <span
            onClick={() => linkToGitlab(commitUrl)}
            className={`${prefixCls}-commit-title-sha`}
          >
            <span>{commitSha ? commitSha.slice(0, 8) : null}</span>
          </span>
        </div>
        <div className={`${prefixCls}-commit-content`}>
          <UserInfo name={userName || '?'} avatar={userHeadUrl} showName={false} />
          <span
            onClick={() => linkToGitlab(commitUrl)}
            className={`${prefixCls}-commit-content-text`}
          >
            <span>{commitContent}</span>
          </span>
        </div>
      </div>
    );
  }

  if (!record) {
    return <Spin />;
  }

  return (<div className={`${prefixCls}`}>
    <Form
      dataSet={detailDs}
      labelLayout="horizontal"
      labelAlign="left"
      labelWidth={110}
    >
      <Output name="pipelineName" renderer={renderPipelineName} />
      <Output name="appServiceName" renderer={appServiceName} />
      <Output name="status" renderer={({ value }) => <StatusTag status={value} size={12} />} />
      <Output name="userDTO" renderer={renderUser} />
      <Output name="createdDate" renderer={renderCreateDate} />
      <Output name="durationSeconds" renderer={renderDuration} />
      <Output name="commit" renderer={renderCommit} />
    </Form>
  </div>);
});
