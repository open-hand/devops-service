import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Table, Select } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import filter from 'lodash/filter';

import './index.less';

const { Column } = Table;

const EditForm = injectIntl(observer(({ tableDs, selectedDs, intl: { formatMessage }, intlPrefix, prefixCls, modal }) => {
  useEffect(() => {
    tableDs.query();
  }, []);

  modal.handleOk(() => {
    const records = filter(tableDs.selected, (item) => !!item.get('appId'));
    selectedDs.push(...records);
  });

  function renderShare({ value, record }) {
    if (!record.get('appId')) {
      return <FormattedMessage id={`${intlPrefix}.source.${value}`} />;
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
      <Column name="share" renderer={renderShare} />
    </Table>
  );
}));

export default EditForm;
