import React, { Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, TextField, TextArea, Select } from 'choerodon-ui/pro';
import YamlEditor from '../../../../../../../components/yamlEditor';
import { useFormStore } from './stores';

import './index.less';

const { Option } = Select;

function DeployConfigForm() {
  const {
    isEdit,
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    dataSet,
    appOptionDs,
    modal,
    envId,
    refresh,
    store,
  } = useFormStore();
  const [value, setValue] = useState('');
  const [isError, setValueError] = useState(false);

  async function handleSubmit() {
    if (isError) return false;

    const config = value || store.getValue || '';
    const record = dataSet.current;
    if (record) {
      record.set('value', config);
      record.set('envId', envId);
    }
    try {
      if ((await dataSet.submit()) !== false) {
        store.setTabKey('config');
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
    const record = dataSet.current;
    const app = record && record.get('appServiceId');
    const configValue = store.getValue;
    return app ? <YamlEditor
      readOnly={false}
      value={value || configValue}
      originValue={configValue}
      onValueChange={setValue}
      handleEnableNext={setValueError}
    /> : null;
  }

  return <Fragment>
    <div className={`${prefixCls}-config-form`}>
      <Form dataSet={dataSet}>
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

export default observer(DeployConfigForm);
