import React from 'react';
import { Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import TimePopover from '../../../../../components/time-popover';
import { useEnvironmentStore } from '../../../stores';
import { useDetailStore } from './stores';

const { Column } = Table;

export default function Permissions() {
  const {
    envStore: {
      getSelectedMenu: { active },
    },
  } = useEnvironmentStore();
  const {
    intl: { formatMessage },
    permissionsDs: tableDs,
  } = useDetailStore();

  function renderActions({ record }) {
    const role = record.get('role');
    const actionData = [
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: () => {
          tableDs.delete(record);
        },
      },
    ];
    return role === 'member' ? <Action data={actionData} /> : null;
  }

  function renderDate({ value }) {
    return value ? <TimePopover datetime={value} /> : null;
  }

  function renderRole({ value }) {
    return formatMessage({ id: value });
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
      <Column name="role" renderer={renderRole} />
      <Column name="creationDate" renderer={renderDate} />
    </Table>
  );
}
