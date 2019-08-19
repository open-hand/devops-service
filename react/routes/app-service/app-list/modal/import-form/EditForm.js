import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Form, TextField, Select } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';

const { Option } = Select;

const EditForm = injectIntl(observer(({ record }) => (
  <Form record={record}>
    <TextField name="name" />
    <TextField name="code" />
    <Select
      name="version"
      searchable
      clearButton={false}
    />
  </Form>
)));

export default EditForm;
