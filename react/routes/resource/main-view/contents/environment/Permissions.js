import React from 'react';
import { Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import { FormattedMessage } from 'react-intl';
import TimePopover from '../../../../../components/time-popover';
import { useEnvironmentStore } from './stores';

const { Column } = Table;

export default function Permissions() {
  const {
    intl: { formatMessage },
    permissionsDs: tableDs,
    baseInfoDs,
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
    const isOwner = record.get('role') === 'member';
    return isOwner && <Action data={actionData} />;
  }

  function renderDate({ value }) {
    return value && <TimePopover datetime={value} />;
  }

  function renderRole({ value }) {
    return value && <FormattedMessage id={value} />;
  }

  function getActionColumn() {
    const envRecord = baseInfoDs.current;
    const isSkip = envRecord.get('skipCheckPermission');
    const synchro = envRecord.get('synchronize');
    const isDisplay = !isSkip && synchro;
    return isDisplay && <Column renderer={renderActions} />;
  }

  return (
    <Table
      dataSet={tableDs}
      border={false}
      queryBar="bar"
    >
      <Column name="realName" />
      {getActionColumn()}
      <Column name="loginName" />
      <Column name="role" renderer={renderRole} />
      <Column name="creationDate" renderer={renderDate} />
    </Table>
  );
}
