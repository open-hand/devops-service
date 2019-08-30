import React, { Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, TextField, TextArea, Select } from 'choerodon-ui/pro';
import YamlEditor from '../../../../../../../components/yamlEditor';
import { useFormStore } from './stores';

import './index.less';

const { Option } = Select;

function DeployConfig() {
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    formDs,
    appOptionDs,
    configStore: { getValue },
    modal,
    envId,
    refresh,
    parentStore,
  } = useFormStore();
  const [value, setValue] = useState('');
  const [isError, setValueError] = useState(false);

  async function handleSubmit() {
    if (isError) return false;

    const config = value || getValue || '';
    const record = formDs.current;
    if (record) {
      record.set('value', config);
      record.set('envId', envId);
    }
    try {
      if ((await formDs.submit()) !== false) {
        parentStore.setTabKey('config');
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  modal.handleOk(handleSubmit);

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
      onValueChange={setValue}
      handleEnableNext={setValueError}
    /> : null;
  }

  return <Fragment>
    <div className={`${prefixCls}-config-form`}>
      <Form dataSet={formDs}>
        <TextField name="name" />
        <TextArea name="description" resize="vertical" />
        <Select
          searchable={false}
          name="appServiceId"
        >
          {appOptionDs.map(appOption)}
        </Select>
      </Form>
    </div>
    <h3>{formatMessage({ id: `${intlPrefix}.config` })}</h3>
    {renderValue()}
  </Fragment>;
}

export default observer(DeployConfig);
