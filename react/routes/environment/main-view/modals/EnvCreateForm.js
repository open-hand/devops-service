import React from 'react';
import { observer } from 'mobx-react-lite';
import { Form, TextField, Select, TextArea } from 'choerodon-ui/pro';
import StatusDot from '../../../../components/status-dot';

const { Option } = Select;

const getClusterOption = (record) => {
  const id = record.get('id');
  const name = record.get('name');
  const connect = record.get('connect');

  return <Option key={id} value={id}>
    <div>
      <StatusDot active synchronize connect={connect} />
      {name}
    </div>
  </Option>;
};

function EnvCreateForm({ modal, dataSet, clusterDs }) {
  modal.handleOk(() => {
    // console.log(dataSet.data);
  });

  return <Form dataSet={dataSet}>
    <Select name="cluster">
      {clusterDs.map(getClusterOption)}
    </Select>
    <TextField name="code" />
    <TextField name="name" />
    <TextArea name="description" />
    <Select name="group">
      <Option value="zh-cn">简体中文</Option>
      <Option value="en-us">英语(美国)</Option>
      <Option value="ja-jp">日本語</Option>
    </Select>
  </Form>;
}

export default observer(EnvCreateForm);
