import React, { Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Select, Radio } from 'choerodon-ui';
import { Form } from 'choerodon-ui';
import omit from 'lodash/omit';
import map from 'lodash/map';
import DynamicSelect from '../../../../../../../components/dynamic-select';
import { handlePromptError } from '../../../../../../../utils';

import './index.less';

const FormItem = Form.Item;
const { Option } = Select;
const RadioGroup = Radio.Group;

const Permission = observer(({ modal, form, store, onOk, skipPermission, refresh, intlPrefix, prefixCls, intl: { formatMessage } }) => {
  const { getFieldDecorator } = form;
  const { getUsers } = store;
  const [isSkip, setIsSkip] = useState(skipPermission);

  modal.handleOk(async () => {
    let users = null;
    form.validateFields((err, values) => {
      if (!err) {
        const selectedUsers = omit(values, ['keys', 'skipCheckPermission']);
        const skipCheckPermission = values.skipCheckPermission;
        const userIds = Object.values(selectedUsers);
        users = {
          skipCheckPermission,
          userIds,
        };
      }
    });

    if (!users || !users.userIds) return false;

    try {
      const res = await onOk(users);
      if (!handlePromptError(res, false)) return false;
      refresh();
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  });

  function handleChange(e) {
    setIsSkip(e.target.value);
  }

  function getSelector() {
    if (isSkip) return null;

    const { getFieldsValue } = form;
    const data = getFieldsValue();

    const options = map(getUsers, ({ iamUserId, realName }) => {
      const selectedValues = Object.values(omit(data, 'keys'));
      return <Option
        disabled={selectedValues.includes(iamUserId)}
        key={iamUserId}
        value={iamUserId}
      >{realName}</Option>;
    });

    return <DynamicSelect
      options={options}
      form={form}
      fieldKeys={data}
      requireText={formatMessage({ id: `${intlPrefix}.project.member.require` })}
      notFoundContent={formatMessage({ id: `${intlPrefix}.project.member.empty` })}
      label={formatMessage({ id: `${intlPrefix}.project.member` })}
      addText={formatMessage({ id: `${intlPrefix}.add.member` })}
    />;
  }

  form.getFieldDecorator('keys', { initialValue: ['key0'] });
  return (
    <Fragment>
      <div className={`${prefixCls}-modal-head`}>{formatMessage({ id: `${intlPrefix}.set-operator` })}</div>
      <Form>
        <div className={`${prefixCls}-modal-selectbox`}>
          <FormItem>
            {getFieldDecorator('skipCheckPermission', { initialValue: isSkip })(
              <RadioGroup onChange={handleChange}>
                <Radio value>
                  {formatMessage({ id: `${intlPrefix}.member.all` })}
                </Radio>
                <Radio value={false}>
                  {formatMessage({ id: `${intlPrefix}.member.specific` })}
                </Radio>
              </RadioGroup>
            )}
          </FormItem>
        </div>
        {getSelector()}
      </Form>
    </Fragment>
  );
});

export default Form.create()(injectIntl(Permission));
