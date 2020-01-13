import React, { useMemo } from 'react';
import { Choerodon } from '@choerodon/boot';
import { Form, Password, Select, TextField } from 'choerodon-ui/pro';
import { filter, forEach, map } from 'lodash';
import { observer } from 'mobx-react-lite';
import { usePrometheusStore } from './stores';

import './index.less';

export default observer((props) => {
  const {
    formDs,
    modal,
    refresh,
    prefixCls,
    intl: { formatMessage },
    intlPrefix,
    pvSelect,
    pvSelectEdit,
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
    const arr = filter(pvSelect, (item) => item !== name);
    let result = true;
    forEach(arr, (item) => {
      if (pvId === record.get(item)) {
        result = false;
      }
    });
    return result;
  }

  function getSelectContent(item) {
    const record = formDs.current;
    if (isModify && record.getPristineValue(pvSelectEdit[item])) {
      return <TextField name={pvSelectEdit[item]} key={item} disabled />;
    } else {
      return (
        <Select
          key={item}
          name={item}
          searchable
          optionsFilter={(optionRecord) => handlePvFilter(optionRecord, item)}
        />
      );
    }
  }

  return (
    <div>
      <Form dataSet={formDs}>
        {!isModify && <Password name="adminPassword" autoFocus />}
        <TextField name="grafanaDomain" autoFocus={isModify} />
      </Form>
      <div className={`${prefixCls}-monitor-create-pv`}>
        <span>{formatMessage({ id: `${intlPrefix}.monitor.pv` })}</span>
      </div>
      <Form dataSet={formDs}>
        {map(pvSelect, (item) => getSelectContent(item))}
      </Form>
    </div>
  );
});
