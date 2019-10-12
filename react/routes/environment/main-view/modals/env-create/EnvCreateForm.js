import React, { Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { Form as ProForm, TextField, TextArea, Select as ProSelect } from 'choerodon-ui/pro';
import { Select, Form } from 'choerodon-ui';
import StatusDot from '../../../../../components/status-dot';
import { useFormStore } from './stores';
import Tips from '../../../../../components/new-tips';

import './index.less';

const ProOption = ProSelect.Option;
const Option = Select.Option;
const FormItem = Form.Item;

function EnvCreateForm({ modal, form, refresh, intlPrefix }) {
  const {
    intl: { formatMessage },
    formDs,
    clusterOptionDs,
    groupOptionDs,
  } = useFormStore();

  async function handleCreate() {
    let result = true;
    form.validateFields(['clusterId'], (errors) => {
      result = !errors;
    });
    if (!result) {
      return false;
    }

    try {
      if ((await formDs.submit()) !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  modal.handleOk(handleCreate);

  function getClusterOption(record) {
    const id = record.get('id');
    const name = record.get('name');
    const connect = record.get('connect');

    return <Option key={id}>
      <StatusDot
        active
        synchronize
        size="inner"
        connect={connect}
      />
      {name}
    </Option>;
  }

  function getGroupOption(record) {
    const id = record.get('id');
    const name = record.get('name');
    return <ProOption key={id} value={id}>
      {name}
    </ProOption>;
  }

  function handleSelect(value) {
    formDs.current.set('clusterId', Number(value));
  }

  return <div className="c7ncd-env-form-wrap">
    <Form className="c7ncd-env-form">
      <FormItem>
        {form.getFieldDecorator('clusterId', {
          rules: [
            {
              required: true,
              message: formatMessage({ id: 'required' }),
            },
          ],
        })(
          <Select
            allowClear={false}
            filter
            onSelect={handleSelect}
            getPopupContainer={(triggerNode) => triggerNode.parentNode}
            filterOption={(input, option) => option.props.children[1]
              .toLowerCase()
              .indexOf(input.toLowerCase()) >= 0}
            label={formatMessage({ id: 'c7ncd.env.cluster.select' })}
          >
            {clusterOptionDs.map(getClusterOption)}
          </Select>,
        )}
      </FormItem>
    </Form>
    <ProForm dataSet={formDs}>
      <TextField
        name="code"
        addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.code.tips` })} />}
      />
      <TextField
        name="name"
        addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.name.tips` })} />}
      />
      <TextArea name="description" resize="vertical" />
      <ProSelect
        name="devopsEnvGroupId"
        addonAfter={<Tips helpText={formatMessage({ id: `${intlPrefix}.group.tips` })} />}
      >
        {groupOptionDs.map(getGroupOption)}
      </ProSelect>
    </ProForm>
  </div>;
}

export default Form.create({})(observer(EnvCreateForm));
