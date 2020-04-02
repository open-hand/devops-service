import React from 'react';
import { Form, Select } from 'choerodon-ui/pro';
import { useExecuteContentStore } from './stores';

export default () => {
  const {
    selectDs,
  } = useExecuteContentStore();

  return (
    <Form dataSet={selectDs} style={{ width: 340 }}>
      <Select
        name="branch"
        searchable
        clearButton={false}
      />
    </Form>
  );
};
