import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Form, Select } from 'choerodon-ui';
import omit from 'lodash/omit';
import map from 'lodash/map';
import DynamicSelect from '../../../../components/dynamic-select';
import { handlePromptError } from '../../../../../../../utils';

import './index.less';

const LinkService = observer(({ modal, form, store, tree, onOk, intlPrefix, intl: { formatMessage } }) => {
  const { getFieldsValue } = form;
  const { getServices } = store;

  modal.handleOk(async () => {
    let servers = null;
    form.validateFields((err, values) => {
      if (!err) {
        const selectedService = omit(values, ['keys']);
        servers = Object.values(selectedService);
      }
    });

    if (!servers) return false;

    try {
      const res = await onOk(servers);
      if (!handlePromptError(res)) return false;

      tree.query();
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  });

  const data = useMemo(() => getFieldsValue(), [getFieldsValue()]);
  const options = useMemo(() => map(getServices, ({ id, name }) => {
    const selectedValues = Object.values(omit(data, 'keys'));
    return <Select.Option
      disabled={selectedValues.includes(id)}
      key={id}
      value={id}
    >{name}</Select.Option>;
  }), [data, getServices]);
  form.getFieldDecorator('keys', { initialValue: ['key0'] });
  return (
    <Form>
      <DynamicSelect
        options={options}
        form={form}
        fieldKeys={data}
        label={formatMessage({ id: `${intlPrefix}.app-service` })}
        addText={formatMessage({ id: `${intlPrefix}.environment.add.service` })}
      />
    </Form>
  );
});

export default Form.create()(injectIntl(LinkService));
