import React, { useMemo } from 'react';
import { Choerodon } from '@choerodon/boot';
import { Form, Password, Select, TextField } from 'choerodon-ui/pro';
import { usePrometheusStore } from './stores';

import './index.less';

export default (props) => {
  const {
    formDs,
    modal,
    refresh,
    prefixCls,
    intl: { formatMessage },
    intlPrefix,
  } = usePrometheusStore();

  const isModify = useMemo(() => {
    if (formDs.current) {
      return formDs.current.status !== 'add';
    }
    return false;
  }, [formDs.current]);

  modal.handleOk(() => {
    try {
      if (formDs.submit() !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  });

  return (
    <div>
      <Form dataSet={formDs}>
        <Password name="adminPassword" autoFocus />
        <TextField name="grafanaDomain" />
      </Form>
      <div className={`${prefixCls}-monitor-create-pv`}>
        <span>{formatMessage({ id: `${intlPrefix}.monitor.pv` })}</span>
      </div>
      <Form dataSet={formDs}>
        <Select name="prometheusPV" searchable disabled={isModify} />
        <Select name="grafanaPV" searchable disabled={isModify} />
        <Select name="alertManagerPV" searchable disabled={isModify} />
      </Form>
    </div>
  );
};
