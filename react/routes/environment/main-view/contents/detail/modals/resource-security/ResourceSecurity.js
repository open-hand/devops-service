import React, { Fragment, useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import _ from 'lodash';

import { Action } from '@choerodon/boot';
import { Table, Button, Modal } from 'choerodon-ui/pro';

import ClickText from '../../../../../../../components/click-text';
import TableTags from './components/tableTags';
import UserList from './components/userList';
import { useResourceSecurityStore } from './stores';
import ResourceSecurityForm from './resource-security-form';

import './index.less';

const { Column } = Table;
const formKey = Modal.key();
const modalStyle = {
  width: 380,
};

export default observer((props) => {
  const resourceSecurityStore = useResourceSecurityStore();
  const { resourceSecurityDs, formatMessage, resourceSecurityCreateDs } = resourceSecurityStore;

  const renderNumber = ({ value: id }) => <ClickText value={`#${id}`} clickAble record={id} onClick={() => { openSiderBar('edit', id); }} />;
  function renderTags(data, type) {
    return _.map(data, (item) => <TableTags
      key={item}
      value={formatMessage({ id: `notification.${type}.${item}` })}
    />);
  }
  const renderEvent = ({ record }) => renderTags(record.get('notifyTriggerEvent'), 'event');
  const renderMethod = ({ record }) => renderTags(record.get('notifyType'), 'method');
  const renderUser = ({ record }) => (
    <div className="c7n-devops-userlist-warp">
      <UserList
        type={record.get('notifyObject')}
        dataSource={record.get('userRelDTOS')}
      />
    </div>
  );
  const renderAction = ({ record }) => {
    const actionData = [
      {
        service: ['devops-service.devops-notification.delete'],
        text: formatMessage({ id: 'delete' }),
        action: () => {
          const modalProps = {
            title: formatMessage({ id: 'notification.delete' }),
            children: formatMessage({ id: 'notification.delete.message' }),
            okText: formatMessage({ id: 'delete' }),
            okProps: { color: 'red' },
            cancelProps: { color: 'dark' },
          };
          resourceSecurityDs.delete(record, modalProps);
        },
      },
    ];
    return <Action data={actionData} />;
  };

  function openSiderBar(type, id) {
    const modalProps = {
      store: resourceSecurityStore,
      notificationId: id,
      type,
    };
    resourceSecurityCreateDs.reset();
    resourceSecurityCreateDs.create();
    Modal.open({
      key: formKey,
      title: formatMessage({ id: `notification.sidebar.${type}` }),
      children: <ResourceSecurityForm {...modalProps} />,
      drawer: true,
      okText: formatMessage({ id: type }),
      style: modalStyle,
    });
  }

  return (
    <Fragment>
      <Button
        funcType="flat"
        icon="playlist_add"
        className="header-btn"
        onClick={() => { openSiderBar('create'); }}
      >
        {formatMessage({ id: 'c7ncd.env.resource.setting.create' })}
      </Button>
      <Table
        dataSet={resourceSecurityDs}
        border={false}
        queryBar="none"
      >
        <Column width={50} name="id" header={formatMessage({ id: 'number' })} renderer={renderNumber} />
        <Column width={50} renderer={renderAction} />
        <Column renderer={renderEvent} header={formatMessage({ id: 'notification.event' })} />
        <Column renderer={renderMethod} header={formatMessage({ id: 'notification.method' })} />
        <Column renderer={renderUser} header={formatMessage({ id: 'notification.target' })} />
      </Table>
    </Fragment>
  );
});
