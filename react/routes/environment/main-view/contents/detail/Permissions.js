import React from 'react';
import { Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import TimePopover from '../../../../../components/time-popover';
import { useDetailStore } from './stores';

const { Column } = Table;

export default function Permissions() {
  const {
    intl: { formatMessage },
    permissionsDs: tableDs,
  } = useDetailStore();

  function renderActions({ record }) {
    const actionData = [
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: () => {
          tableDs.delete(record);
        },
      },
    ];
    return (<Action data={actionData} />);
  }

  function renderDate({ value }) {
    return value ? <TimePopover datetime={value} /> : null;
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
