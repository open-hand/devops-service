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
    intlPrefix,
    prefixCls,
    detailDs,
  } = useRecordDetailStore();

  const record = useMemo(() => detailDs.current, [detailDs.current]);

  function renderUser({ value }) {
    const { realName, imageUrl } = value || {};
    return <UserInfo name={realName} avatar={imageUrl} />;
  }

  function renderDuration({ value }) {
    return getDuration({ value, unit: 's' });
  }

  function renderPipelineName() {
    const { name } = record.get('devopsCiPipelineVO') || {};
    return name;
  }

  function appServiceName() {
    const { appServiceName: name } = record.get('devopsCiPipelineVO') || {};
    return name;
  }
  
  function renderCommit({ value }) {
    const { commitContent, commitSha, commitUrl, ref, userHeadUrl, userName, gitlabProjectUrl } = value || {};
    return (
      <div className={`${prefixCls}-commit`}>
        <div className={`${prefixCls}-commit-title`}>
          <Icon type="branch" className={`${prefixCls}-commit-title-branch`} />
          <a
            href={`${gitlabProjectUrl}/commits/${ref}`}
            target="_blank"
            rel="nofollow me noopener noreferrer"
            className={`${prefixCls}-commit-title-ref`}
          >
            <span>{ref}</span>
          </a>
          <Icon type="point" className={`${prefixCls}-commit-title-point`} />
          <a
            href={commitUrl}
            target="_blank"
            rel="nofollow me noopener noreferrer"
            className={`${prefixCls}-commit-title-sha`}
          >
            <span>{commitSha ? commitSha.slice(0, 8) : null}</span>
          </a>
        </div>
        <div className={`${prefixCls}-commit-content`}>
          <UserInfo name={userName || '?'} avatar={userHeadUrl} showName={false} />
          <a
            href={commitUrl}
            target="_blank"
            rel="nofollow me noopener noreferrer"
            className={`${prefixCls}-commit-content-text`}
          >
            <span>{commitContent}</span>
          </a>
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
      <Output name="finishedDate" />
      <Output name="durationSeconds" renderer={renderDuration} />
      <Output name="commit" renderer={renderCommit} />
    </Form>
  </div>);
});
