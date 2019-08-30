import React from 'react';
import { observer } from 'mobx-react-lite';
import { TextField, TextArea, Select, Form } from 'choerodon-ui/pro';
import { useFormStore } from './stores';

const Option = Select.Option;

function EnvModifyForm() {
  const {
    formDs,
    groupOptionDs,
    modal,
    refresh,
  } = useFormStore();

  async function handleSubmit() {
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

  modal.handleOk(handleSubmit);

  function getGroupOption(record) {
    const id = record.get('id');
    const name = record.get('name');
    return <Option key={id} value={id}>
      {name}
    </Option>;
  }

  return <Form dataSet={formDs}>
    <TextField name="name" />
    <TextArea name="description" resize="vertical" />
    <Select name="devopsEnvGroupId">
      {groupOptionDs.map(getGroupOption)}
    </Select>
  </Form>;
}

export default observer(EnvModifyForm);
