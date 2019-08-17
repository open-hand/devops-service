import React, { useMemo, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Form } from 'choerodon-ui';
import DynamicSelect from '../../../../components/dynamic-select';

import './index.less';

const LinkService = observer(({ modal, form, store, intlPrefix, intl: { formatMessage } }) => {
  const { getFieldsValue } = form;
  const { getServiceData } = store;

  modal.handleOk(() => false);

  useEffect(() => {
    form.getFieldDecorator('keys', { initialValue: ['key0'] });
    return () => null;
  }, []);

  const data = useMemo(() => getFieldsValue(), [getFieldsValue()]);
  return (
    <Form>
      <DynamicSelect
        optionData={getServiceData}
        form={form}
        fieldKeys={data}
        label={formatMessage({ id: `${intlPrefix}.app-service` })}
        addText={formatMessage({ id: `${intlPrefix}.environment.add.service` })}
      />
    </Form>
  );
});

export default Form.create()(injectIntl(LinkService));
