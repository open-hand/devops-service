import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Table, Select } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Checkbox } from 'choerodon-ui';
import forEach from 'lodash/forEach';

const { Option } = Select;
const { Column } = Table;

const EditForm = injectIntl(observer(({ tableDs, intl: { formatMessage }, intlPrefix, prefixCls }) => {
  function renderShare({ value, record }) {
    if (!record.get('appId')) {
      return <FormattedMessage id={`${intlPrefix}.source.${value}`} />;
    }
  }

  function renderSelect({ value, record }) {
    return (
      <Checkbox
        checked={value}
        onChange={(e) => handleSelected(e, record)}
      />
    );
  }

  function handleSelected(e, record) {
    record.set('selected', e.target.checked);
    if (!record.get('appId')) {
      tableDs.forEach((eachRecord) => {
        if (eachRecord.get('appId') === record.get('id')) {
          eachRecord.set('selected', e.target.checked);
        }
      });
    }
  }

  return (
    <Table
      dataSet={tableDs}
      mode="tree"
      defaultRowExpanded
      expandIconColumnIndex={1}
      queryBar="none"
    >
      <Column name="selected" renderer={renderSelect} width=".5rem" />
      <Column name="name" />
      <Column name="share" renderer={renderShare} />
    </Table>
  );
}));

export default EditForm;
