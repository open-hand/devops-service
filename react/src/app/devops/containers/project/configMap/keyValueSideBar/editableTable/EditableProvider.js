/**
 * @author ale0720@163.com
 * @date 2019-05-09 22:12
 */
import React from 'react';
import { Form } from 'choerodon-ui';
import { Provider } from './EditableContext';

const EditableRow = ({ form, index, ...props }) => (
  <Provider value={form}>
    <tr {...props} />
  </Provider>
);

export const EditableFormRow = Form.create()(EditableRow);
