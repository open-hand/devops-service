import React, { Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, TextField, TextArea, Select } from 'choerodon-ui/pro';
import YamlEditor from '../../../../../../../components/yamlEditor';
import { useFormStore } from './stores';

import './index.less';

const { Option } = Select;

function DeployConfig({ modal }) {
  const {
    prefixCls,
    intl: { formatMessage },
    formDs,
    appOptionDs,
    configStore: { getValue },
  } = useFormStore();
  const [value, setValue] = useState('');
  const [isError, setValueError] = useState(false);

  async function handleSubmit() {
    try {
      if ((await formDs.submit()) !== false) {
        // refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  function changeValue(data) {
    setValue(value);
    const record = formDs.current;
    if (record) {
      record.set('value', data);
    }
  }

  // modal.handleOk(handleSubmit);

  function appOption(record) {
    const id = record.get('id');
    const name = record.get('name');
    return <Option key={id} value={id}>
      {name}
    </Option>;
  }

  function renderValue() {
    const record = formDs.current;
    const app = record && record.get('appServiceId');
    return app ? <YamlEditor
      readOnly={false}
      value={value || getValue}
      originValue={getValue}
      onValueChange={changeValue}
      handleEnableNext={setValueError}
    /> : null;
  }

  return <Fragment>
    <div className={`${prefixCls}-config-form`}>
      <Form dataSet={formDs}>
        <TextField name="name" />
        <TextArea name="description" resize="vertical" />
        <Select
          allowClear={false}
          searchable={false}
          name="appServiceId"
        >
          {appOptionDs.map(appOption)}
        </Select>
      </Form>
    </div>
    <h3>配置信息</h3>
    {renderValue()}
  </Fragment>;
}

export default observer(DeployConfig);
