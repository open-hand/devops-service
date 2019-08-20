import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Form, TextField, Select } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';

const { Option } = Select;

const EditForm = injectIntl(observer(({ record, versionOptions, projectId }) => {
  useEffect(() => {
    record.getField('version').set('options', versionOptions);
    versionOptions.transport.read.url = `/devops/v1/projects/${projectId}/app_service_versions/${record.get('id')}/list_share_versions${record.get('share') ? '?share=share' : ''}`;
    versionOptions.query();
  }, []);

  return (
    <Form record={record}>
      <TextField name="name" />
      <TextField name="code" />
      <Select
        name="version"
        searchable
        clearButton={false}
      />
    </Form>
  );
}));

export default EditForm;
