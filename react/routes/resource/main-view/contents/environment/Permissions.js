import React from 'react';
import { Permission, Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import { FormattedMessage } from 'react-intl';
import TimePopover from '../../../../../components/timePopover';
import { useEnvironmentStore } from './stores';

const { Column } = Table;

export default function Permissions() {
  const {
    intl: { formatMessage },
    permissionsDs: tableDs,
  } = useEnvironmentStore();

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
    return record.get('role') === 'member' && <Action data={actionData} />;
  }

  function renderDate({ value }) {
    return value && <TimePopover content={value} />;
  }

  function renderRole({ value }) {
    return value && <FormattedMessage id={value} />;
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
