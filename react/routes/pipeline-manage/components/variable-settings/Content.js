import React, { useState, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Modal, Spin, Icon, TextField, Button, Tooltip, TextArea } from 'choerodon-ui/pro';
import { useRecordDetailStore } from './stores';

import './index.less';

export default observer(() => {
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    formDs,
    modal,
    appServiceName,
    appServiceId,
  } = useRecordDetailStore();
  const [showRevealValues, changeShowRevealValues] = useState(false);

  modal.handleOk(async () => {
    try {
      const res = await formDs.submit();
      if (res !== false) {
        return true;
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  });

  modal.handleCancel(() => {
    if (formDs.dirty) {
      Modal.open({
        key: Modal.key(),
        title: formatMessage({ id: 'prompt.inform.title' }),
        children: formatMessage({ id: 'prompt.inform.message' }),
        onOk: () => modal.close(),
      });
      return false;
    }
  });

  function handleAdd() {
    formDs.create();
  }

  function handleRemove(eachRecord) {
    formDs.remove(eachRecord);
  }

  function renderValue({ value }) {
    if (value) {
      return showRevealValues ? value : '**********';
    }
  }

  return (<div className={`${prefixCls}`}>
    <Form style={{ width: '70%' }}>
      {appServiceId ? <TextField
        value={appServiceName}
        label="关联应用服务"
        disabled
      /> : null}
    </Form>
    <Button
      funcType="flat"
      color="primary"
      className={`${prefixCls}-reveal`}
      onClick={() => changeShowRevealValues(!showRevealValues)}
    >
      {formatMessage({ id: `${intlPrefix}.settings.values.${showRevealValues ? 'hide' : 'reveal'}` })}
    </Button>
    {formDs.map((eachRecord) => (
      <Form record={eachRecord} columns={14} key={eachRecord.id}>
        <TextField colSpan={5} name="key" />
        <span className={`${prefixCls}-equal`}>=</span>
        <TextArea
          colSpan={7}
          name="value"
          renderer={renderValue}
          resize="vertical"
          autoSize={{ minRows: 1 }}
        />
        {formDs.length > 1 ? (
          <Tooltip title={formatMessage({ id: 'delete' })}>
            <Button
              funcType="flat"
              icon="delete"
              onClick={() => handleRemove(eachRecord)}
            />
          </Tooltip>
        ) : <span />}
      </Form>
    ))}
    <Button
      funcType="flat"
      color="primary"
      icon="add"
      onClick={handleAdd}
      className={`${prefixCls}-add-btn`}
    >
      {formatMessage({ id: `${intlPrefix}.settings.add` })}
    </Button>
  </div>);
});
