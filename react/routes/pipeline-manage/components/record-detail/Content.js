import React, { useMemo, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Output, Spin } from 'choerodon-ui/pro';
import { useRecordDetailStore } from './stores';
import StatusTag from '../PipelineFlow/components/StatusTag';
import UserInfo from '../../../../components/userInfo';
import getDuration from '../../../../utils/getDuration';

import './index.less';

export default observer(() => {
  const {
    intl: { formatMessage },
    intlPrefix,
    detailDs,
  } = useRecordDetailStore();

  const record = useMemo(() => detailDs.current, [detailDs.current]);

  function renderUser({ value }) {
    const { realName, imageUrl } = value || {};
    return <UserInfo name={realName} avatar={imageUrl} showName />;
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

  if (!record) {
    return <Spin />;
  }

  return (<div className="c7ncd-pipelineMange-record-detail">
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
    </Form>
  </div>);
});
