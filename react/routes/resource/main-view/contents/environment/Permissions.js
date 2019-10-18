import React from 'react';
import { Action } from '@choerodon/boot';
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
    intlPrefix,
  } = useEnvironmentStore();

  function handleDelete() {
    const record = tableDs.current;
    const modalProps = {
      title: formatMessage({ id: `${intlPrefix}.permission.delete.title` }),
      children: formatMessage({ id: `${intlPrefix}.permission.delete.des` }),
      okText: formatMessage({ id: 'delete' }),
      okProps: { color: 'red' },
      cancelProps: { color: 'dark' },
    };
    tableDs.delete(record, modalProps);
  }

  function renderActions({ record }) {
    const actionData = [
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
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
    return !isSkip && <Column renderer={renderActions} />;
  }

  return (
    <div className="c7ncd-tab-table">
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
    </div>
  );
}
