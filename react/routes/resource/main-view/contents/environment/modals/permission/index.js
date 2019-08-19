import React, { Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Select, SelectBox } from 'choerodon-ui/pro';
import { Form } from 'choerodon-ui';
import omit from 'lodash/omit';
import map from 'lodash/map';
import DynamicSelect from '../../../../components/dynamic-select';
import { handlePromptError } from '../../../../../../../utils';

import './index.less';

const FormItem = Form.Item;
const { Option } = Select;

const Permission = observer(({ modal, form, store, tree, onOk, intlPrefix, prefixCls, intl: { formatMessage } }) => {
  const { getFieldDecorator } = form;
  const { getUsers } = store;
  const [isSkip, setIsSkip] = useState(true);

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

    if (!users.userIds) return false;

    try {
      const res = await onOk(users);
      if (!res || !handlePromptError(res)) return false;

      tree.query();
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  });


  // TODO: 替换掉 SelectBox
  function handleChange(value) {
    setIsSkip(value);
  }

  function getSelector() {
    if (isSkip) return null;

    const { getFieldsValue } = form;
    const data = getFieldsValue();

    const options = map(getUsers, ({ iamUserId, realName }) => {
      const selectedValues = Object.values(omit(data, 'keys'));
      return <Select.Option
        disabled={selectedValues.includes(iamUserId)}
        key={iamUserId}
        value={iamUserId}
      >{realName}</Select.Option>;
    });

    return <DynamicSelect
      options={options}
      form={form}
      fieldKeys={data}
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
            {getFieldDecorator('skipCheckPermission', { initialValue: isSkip })(<SelectBox
              // readOnly={!getUsers.length}
              onChange={handleChange}
            >
              <Option value>
                {formatMessage({ id: `${intlPrefix}.member.all` })}
              </Option>
              <Option value={false}>
                {formatMessage({ id: `${intlPrefix}.member.specific` })}
              </Option>
            </SelectBox>)}
          </FormItem>
        </div>
        {getSelector()}
      </Form>
    </Fragment>
  );
});

export default Form.create()(injectIntl(Permission));
