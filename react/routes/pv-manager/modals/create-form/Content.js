import React, { useMemo, useState, useEffect, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Choerodon } from '@choerodon/boot';
import { Form, Select, TextField, TextArea, NumberField } from 'choerodon-ui/pro';
import { usePVCreateStore } from './stores';

import './index.less';
import StatusDot from '../../../../components/status-dot';

const CreateForm = () => {
  const {
    formDs,
    intl: { formatMessage },
    modal,
    refresh,
    intlPrefix,
    prefixCls,
  } = usePVCreateStore();

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

  function renderClusterOption({ record, text, value }) {
    return (
      <Fragment>
        {text && <StatusDot
          active
          synchronize
          size="inner"
          connect={record.get('connect')}
        />}
        {text}
      </Fragment>
    );
  }

  function getClusterOptionProp({ record }) {
    return {
      disabled: !record.get('connect'),
    };
  }

  return (
    <div className={`${prefixCls}-create-wrap`}>
      <Form dataSet={formDs} columns={3}>
        <Select
          name="clusterId"
          searchable
          colSpan={3}
          clearButton={false}
          optionRenderer={renderClusterOption}
          onOption={getClusterOptionProp}
        />
        <TextField name="name" colSpan={3} disabled={!formDs.current.get('clusterId')} />
        <TextArea name="description" colSpan={3} resize="vertical" />
        <Select name="type" colSpan={3} clearButton={false} />
        <Select name="accessModes" colSpan={3} clearButton={false} />
        <NumberField name="storage" step={1} colSpan={2} />
        <Select name="unit" clearButton={false} />
        <TextField name="path" colSpan={3} />
        {formDs.current.get('type') === 'NFS' && (
          <TextField name="ip" colSpan={3} />
        )}
      </Form>
    </div>
  );
};

export default injectIntl(observer(CreateForm));
