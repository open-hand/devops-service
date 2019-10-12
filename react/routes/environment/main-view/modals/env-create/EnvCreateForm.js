import React, { Fragment } from 'react';
import { Form, TextField, TextArea, Select } from 'choerodon-ui/pro';
import StatusDot from '../../../../../components/status-dot';
import { useFormStore } from './stores';

import './index.less';

function ClusterItem({ text, connect }) {
  return <Fragment>
    {text && <StatusDot
      active
      synchronize
      size="inner"
      connect={connect}
    />} {text}
  </Fragment>;
}

export default function EnvCreateForm({ modal, refresh }) {
  const {
    formDs,
    clusterOptionDs,
  } = useFormStore();

  async function handleCreate() {
    try {
      if ((await formDs.submit()) !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  modal.handleOk(handleCreate);

  function clusterRenderer({ record, text }) {
    const current = clusterOptionDs.find(item => item.get('id') === record.get('clusterId'));
    if (current) {
      const connect = current.get('connect');
      return <ClusterItem text={text} connect={connect} />;
    }
    return text;
  }

  function getClusterOption({ record, text }) {
    const connect = record.get('connect');
    return <ClusterItem text={text} connect={connect} />;
  }

  function getGroupOption({ text }) {
    return text;
  }

  return <div className="c7ncd-env-form-wrap">
    <Form dataSet={formDs}>
      <Select
        searchable
        name="clusterId"
        renderer={clusterRenderer}
        optionRenderer={getClusterOption}
        clearButton={false}
      />
      <TextField name="code" />
      <TextField name="name" />
      <TextArea name="description" resize="vertical" />
      <Select
        searchable
        name="devopsEnvGroupId"
        optionRenderer={getGroupOption}
        renderer={getGroupOption}
        clearButton={false}
      />
    </Form>
  </div>;
}
