import React from 'react';
import { TextField, TextArea, Select, Form } from 'choerodon-ui/pro';
import { useFormStore } from './stores';

function EnvModifyForm() {
  const {
    formDs,
    modal,
    refresh,
    envStore,
    envId,
  } = useFormStore();

  async function handleSubmit() {
    try {
      if ((await formDs.submit()) !== false) {
        envStore.setUpTarget(envId);
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  modal.handleOk(handleSubmit);

  function getGroupOption({ text }) {
    return text;
  }

  return <Form dataSet={formDs}>
    <TextField name="name" />
    <TextArea name="description" resize="vertical" />
    <Select
      searchable
      name="devopsEnvGroupId"
      renderer={getGroupOption}
      optionRenderer={getGroupOption}
    />
  </Form>;
}

export default EnvModifyForm;
