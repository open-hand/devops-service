import React, { Fragment, useContext, useMemo } from 'react';
import { FormattedMessage } from 'react-intl';
import { Permission, Action } from '@choerodon/boot';
import {
  Table,
  DataSet,
} from 'choerodon-ui/pro';
import TimePopover from '../../../../../../components/timePopover';
import TableDataSet from './stores/TableDataSet';
import Store from '../../../../stores';

const { Column } = Table;

export default function AssignPermissions() {
  const {
    prefixCls,
    intlPrefix,
    intl,
    AppState: {
      currentMenuType: {
        id,
      },
    },
    store: {
      getPreviewData: {
        id: envId,
      },
    },
  } = useContext(Store);
  const tableDs = useMemo(() => new DataSet(TableDataSet({
    intl,
    intlPrefix,
    projectId: id,
    envId,
  })));

  function refresh() {
    tableDs.query();
  }

  function handleDelete(data) {
    tableDs.delete(data);
  }

  function renderActions({ record }) {
    const actionData = [
      {
        service: [],
        text: intl.formatMessage({ id: 'delete' }),
        action: () => handleDelete(record),
      },
    ];
    return (<Action data={actionData} />);
  }

  function renderDate({ value }) {
    return <TimePopover content={value} />;
  }

  return (
    <Table
      dataSet={tableDs}
      border={false}
      queryBar="bar"
    >
      <Column name="realName" />
      <Column name="action" render={renderActions} />
      <Column name="loginName" />
      <Column name="role" />
      <Column name="createDate" render={renderDate} />
    </Table>
  );
}
