import React, { Fragment, useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Form, TextField, TextArea, Select,
} from 'choerodon-ui/pro';
import { ResizeType } from 'choerodon-ui/pro/lib/text-area/enum';
import YamlEditor from '@/components/yamlEditor';
import { useFormStore } from './stores';

import './index.less';

function DeployConfigForm() {
  const {
    formatMessage,
    intlPrefix,
    prefixCls,
    formDs,
    modal,
    configId,
    refresh,
    store,
  } = useFormStore();
  const [isError, setValueError] = useState(false);

  async function handleSubmit() {
    if (isError) return false;

    try {
      const res = await formDs.submit();
      if (res && res.list) {
        const { id } = res.list[0] || {};
        refresh(id);
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }

  modal.handleOk(handleSubmit);

  const changeValue = (value: string) => {
    const record = formDs.current;
    if (record) {
      record.set('value', value);
    }
  };

  function renderValue() {
    const record = formDs.current;
    const app = record && record.get('appServiceId');
    return app && record ? (
      <YamlEditor
        readOnly={false}
        value={record.get('value')}
        originValue={store.getValue || record.getPristineValue('value') || record.get('value')}
        onValueChange={changeValue}
        handleEnableNext={setValueError}
      />
    ) : null;
  }

  return (
    <>
      <div className={`${prefixCls}-config-form`}>
        <Form dataSet={formDs}>
          <TextField name="name" autoFocus />
          <TextArea name="description" resize={'vertical' as ResizeType} />
          {configId
            ? <TextField name="appServiceName" disabled />
            : (
              <Select
                searchable
                name="appServiceId"
              />
            )}
        </Form>
      </div>
      <h3>{formatMessage({ id: `${intlPrefix}.config` })}</h3>
      {renderValue()}
    </>
  );
}

export default observer(DeployConfigForm);
