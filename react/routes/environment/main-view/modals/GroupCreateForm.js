import React from 'react';
import { Form, TextField } from 'choerodon-ui/pro';

export default function ({ modal, dataSet, treeDs }) {
  async function handleCreate() {
    try {
      if ((await dataSet.submit()) !== false) {
        treeDs.query();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  modal.handleOk(handleCreate);

  return <Form dataSet={dataSet}>
    <TextField name="name" />
  </Form>;
}
