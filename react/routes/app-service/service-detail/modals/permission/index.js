import React, { useState, useReducer, useEffect } from 'react';
import { FormattedMessage } from 'react-intl';
import { Radio } from 'choerodon-ui';
import { observer } from 'mobx-react-lite';
import _ from 'lodash';
import './index.less';
import { Button, Select, Form, message } from 'choerodon-ui';
import useStore from '../stores/useStore';


const { Option } = Select;
const RadioGroup = Radio.Group;


export default observer(({ store, record, formatMessage, prefixCls, intlPrefix, nonePermissionDS }) => {
  let nonePermissionRecord;
  useEffect(() => {
    nonePermissionDS.query();
  }, []);
  const { checked, setChecked, permissionUsers, setPermissionUsers } = store;
  return (
    <div>
      <RadioGroup
        label={<FormattedMessage id="app.authority.label" />}
        onChange={() => { setChecked(!checked); }}
        value={checked}
        className={`${prefixCls}-RadioGroup`}
      >
        <Radio value>
          <FormattedMessage id="app.mbr.all" />
        </Radio>
        <Radio value={false}>
          <FormattedMessage id="app.mbr.part" />
        </Radio>  
      </RadioGroup>
      
      {checked ? null 
        : (
          <Form>
            {permissionUsers.map((value, index) => (
              <div className="add-item">
                <Select
                  label={formatMessage({ id: `${intlPrefix}.mbr` })}
                  searchable
                  required
                  value={value}
                  key={value}
                  onChange={(v) => setPermissionUsers({ type: 'change', value: v, index })}
                >
                  {_.map(nonePermissionDS.toData(), ({ iamUserId: id, realName: name }) => (
                    <Option value={id} key={id}>{name}</Option>
                  ))}
                </Select>
                <Button
                  shape="circle"
                  icon="delete"
                  onClick={() => setPermissionUsers({ type: 'sub', value, index })}
                />
              </div>
            ))}
            <Button
              icon="add"
              type="primary"
              onClick={() => { setPermissionUsers({ type: 'add' }); }}
            >
              <FormattedMessage id={`${intlPrefix}.add.mbr`} />
            </Button>
          </Form>)}
    </div>

  );
});

function handleErrorMessage(msg) {
  message.destroy();
  message.config({
    top: 20,
    duration: 1.5,
  });
  message.error(msg, undefined, undefined, 'rightTop');
}
