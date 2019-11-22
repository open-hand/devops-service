import React, { useMemo } from 'react';
import { Choerodon } from '@choerodon/boot';
import { Form, Password, Select, TextField } from 'choerodon-ui/pro';
import filter from 'lodash/filter';
import forEach from 'lodash/forEach';
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

  modal.handleOk(async () => {
    try {
      if (await formDs.submit() !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  });
  
  function handlePvFilter(optionRecord, name) {
    const pvId = optionRecord.get('id');
    const record = formDs.current;
    const arr = filter(['prometheusPvId', 'grafanaPvId', 'alertmanagerPvId'], (item) => item !== name);
    let result = true;
    forEach(arr, (item) => {
      if (pvId === record.get(item)) {
        result = false;
      }
    });
    return result;
  }

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
        <Select name="prometheusPvId" searchable disabled={isModify} optionsFilter={(record) => handlePvFilter(record, 'prometheusPvId')} />
        <Select name="grafanaPvId" searchable disabled={isModify} optionsFilter={(record) => handlePvFilter(record, 'grafanaPvId')} />
        <Select name="alertmanagerPvId" searchable disabled={isModify} optionsFilter={(record) => handlePvFilter(record, 'alertmanagerPvId')} />
      </Form>
    </div>
  );
};
