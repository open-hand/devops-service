import React from 'react';
import { FormattedMessage } from 'react-intl';
import _ from 'lodash';
import './index.less';
import { Select, Form } from 'choerodon-ui';

const Option = Select.Option;

export default function ({ record, formatMessage, prefixCls, intlPrefix }) {
  const handleChange = (value) => {
    // console.log(value);
  };
  return (
    <div>
      <Select
        mode="combobox"
        onChange={handleChange}
        style={{ width: '100%' }}
        placeholder={formatMessage({ id: 'pipeline.task.version' })}
      >
        <Option value="1" key="1">1</Option>
        <Option value="2" key="2">2</Option>
        <Option value="3" key="3">3</Option>
      </Select>
        
    </div>
  );
}
