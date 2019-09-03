import React from 'react';
import { Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import TimePopover from '../../../../../components/time-popover';
import { useEnvironmentStore } from '../../../stores';
import { useDetailStore } from './stores';
import { isNotRunning } from '../../../util';

const { Column } = Table;

function Permissions() {
  const {
    envStore: {
      getSelectedMenu,
    },
  } = useEnvironmentStore();
  const disabled = isNotRunning(getSelectedMenu);
  const {
    intl: { formatMessage },
    permissionsDs: tableDs,
  } = useDetailStore();

  function renderActions({ record }) {
    const { skipCheckPermission } = getSelectedMenu;
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
    const displayAction = !disabled && role === 'member' && !skipCheckPermission;
    return displayAction ? <Action data={actionData} /> : null;
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

export default observer(Permissions);
