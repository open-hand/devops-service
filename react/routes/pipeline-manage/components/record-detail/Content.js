import React, { useMemo, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Output, Spin } from 'choerodon-ui/pro';
import { useRecordDetailStore } from './stores';
import StatusTag from '../PipelineFlow/components/StatusTag';

import './index.less';
import UserInfo from '../../../../components/userInfo';

export default observer(() => {
  const {
    intl: { formatMessage },
    intlPrefix,
    detailDs,
  } = useRecordDetailStore();

  const record = useMemo(() => detailDs.current, [detailDs.current]);

  function renderUser({ value }) {
    return <UserInfo name={value} avatar={record.get('userImageUrl')} showName />;
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
      <Output name="pipelineName" />
      <Output name="appServiceName" />
      <Output name="status" renderer={({ value }) => <StatusTag status={value} size={12} />} />
      <Output name="userName" renderer={renderUser} />
      <Output name="date" />
      <Output name="time" />
    </Form>
  </div>);
});
