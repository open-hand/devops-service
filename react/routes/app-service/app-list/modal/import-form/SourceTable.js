import React, { Fragment, useCallback, useState, useEffect, useMemo } from 'react';
import { Table, Select } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import filter from 'lodash/filter';
import includes from 'lodash/includes';
import forEach from 'lodash/forEach';

import './index.less';

const { Column } = Table;

const SourceTable = injectIntl(observer(({ tableDs, selectedDs, intl: { formatMessage }, intlPrefix, prefixCls, modal }) => {
  const selectedId = useMemo(() => selectedDs.map((record) => record.get('id')), [selectedDs]);

  useEffect(() => {
    loadData();
  }, []);

  modal.handleOk(() => {
    const records = filter(tableDs.selected, (record) => !includes(selectedId, record.get('id')));
    selectedDs.push(...records);
  });

  async function loadData() {
    try {
      if (await tableDs.query() !== false) {
        forEach(selectedId, (id) => {
          tableDs.select(tableDs.find((tableRecord) => tableRecord.get('id') === id));
        });
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
    }
  }

  return (
    <Table
      dataSet={tableDs}
      mode="tree"
      defaultRowExpanded
      queryBar="none"
    >
      <Column name="name" />
      <Column name="code" />
      <Column name="projectName" />
    </Table>
  );
}));

export default SourceTable;
