import React, { useMemo, useState, useEffect, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Choerodon } from '@choerodon/boot';
import { Form, Select, TextField, NumberField } from 'choerodon-ui/pro';
import { usePVCCreateStore } from './stores';

const CreateForm = () => {
  const {
    formDs,
    intl: { formatMessage },
    modal,
    refresh,
    intlPrefix,
    prefixCls,
  } = usePVCCreateStore();

  modal.handleOk(async () => {
    try {
      if (await formDs.submit() !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (error) {
      Choerodon.handleResponseError(error);
      return false;
    }
  });

  function getPvDisabled() {
    const record = formDs.current;
    const canSelect = record && record.get('storage');
    return !canSelect;
  }

  return (
    <div className={`${prefixCls}-pvc-create-wrap`}>
      <Form dataSet={formDs} columns={3}>
        <TextField name="name" colSpan={3} />
        <Select name="accessModes" colSpan={3} clearButton={false} />
        <NumberField name="storage" step={1} colSpan={2} />
        <Select name="unit" clearButton={false} />
        <Select name="type" colSpan={3} clearButton={false} />
        <Select
          name="pvId"
          colSpan={3}
          clearButton={false}
          searchable
          checkValueOnOptionsChange
          disabled={getPvDisabled()}
        />
      </Form>
    </div>
  );
};

export default injectIntl(observer(CreateForm));
