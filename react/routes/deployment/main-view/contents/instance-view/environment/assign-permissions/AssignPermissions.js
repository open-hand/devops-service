import React, { useContext, useCallback } from 'react';
import { Permission, Action } from '@choerodon/boot';
import { Table } from 'choerodon-ui/pro';
import TimePopover from '../../../../../../../components/timePopover';
import Store from './stores';

const { Column } = Table;

export default function AssignPermissions() {
  const {
    intl,
    tableDs,
  } = useContext(Store);

  const renderActions = useCallback(({ record }) => {
    const handleDelete = (data) => {
      tableDs.delete(data);
    };
    const actionData = [
      {
        service: [],
        text: intl.formatMessage({ id: 'delete' }),
        action: () => handleDelete(record),
      },
    ];
    return (<Action data={actionData} />);
  }, [intl, tableDs]);
  const renderDate = useCallback(({ value }) => <TimePopover content={value} />, []);

  function refresh() {
    tableDs.query();
  }

  return (
    <Table
      dataSet={tableDs}
      border={false}
      queryBar="bar"
    >
      <Column name="realName" />
      <Column renderer={renderActions} />
      <Column name="loginName" />
      <Column name="role" />
      <Column name="createDate" renderer={renderDate} />
    </Table>
  );
}
