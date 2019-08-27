import React from 'react';
import { Action } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import TimePopover from '../../../../../components/time-popover';
import UserInfo from '../../../../../components/userInfo';
import { useDetailStore } from './stores';

const { Column } = Table;
const modalKey = Modal.key();

export default function DeployConfig() {
  const {
    intl: { formatMessage },
    configDs,
    currentIntlPrefix,
  } = useDetailStore();

  function openModal() {
    Modal.open({
      key: modalKey,
      title: formatMessage({ id: `${currentIntlPrefix}.modal.env-detail` }),
      children: <div>hello</div>,
      okText: formatMessage({ id: 'close' }),
    });
  }

  function renderActions({ record }) {
    const actionData = [
      {
        service: [],
        text: formatMessage({ id: 'edit' }),
        action: () => {},
      },
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: openModal,
      },
    ];
    return (<Action data={actionData} />);
  }

  function renderUser({ value, record }) {
    const url = record.get('createUserUrl');
    return <UserInfo name={value} avatar={url} />;
  }

  function renderDate({ value }) {
    return value ? <TimePopover datetime={value} /> : null;
  }

  return (
    <Table
      dataSet={configDs}
      border={false}
      queryBar="bar"
    >
      <Column name="name" sortable />
      <Column renderer={renderActions} />
      <Column name="description" sortable />
      <Column name="appServiceName" />
      <Column name="envName" />
      <Column name="createUserRealName" renderer={renderUser} />
      <Column name="lastUpdateDate" renderer={renderDate} />
    </Table>
  );
}
