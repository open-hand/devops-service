import React, { Fragment, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import { Modal, Table } from 'choerodon-ui/pro';
import StatusTag from '../../../../../components/status-tag';
import { getEnvStatus } from '../../../../../components/status-dot';
import { useEnvironmentStore } from '../../../stores';
import { useEnvGroupStore } from './stores';
import Modals from './modals';
import EnvModifyForm from '../../modals/env-modify';

const { Column } = Table;
const envKey = Modal.key;

const Group = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    prefixCls,
    intlPrefix,
    envStore,
    treeDs,
    AppState: { currentMenuType: { id: projectId } },
  } = useEnvironmentStore();
  const {
    groupDs,
    intl: { formatMessage },
  } = useEnvGroupStore();
  const { getSelectedMenu: { id, name } } = envStore;

  function refresh() {
    groupDs.query();
    treeDs.query();
  }

  function handleDelete() {
  }

  function openModifyModal(record) {
    Modal.open({
      key: envKey,
      title: formatMessage({ id: `${intlPrefix}.modify` }),
      children: <EnvModifyForm
        record={record}
        intlPrefix={intlPrefix}
        refresh={refresh}
        envStore={envStore}
      />,
      drawer: true,
      style: modalStyle,
    });
  }

  function renderName({ value, record }) {
    const active = record.get('active');
    const connect = record.get('connect');
    const synchronize = record.get('synchro');
    const status = getEnvStatus(connect, synchronize, active);
    return (
      <Fragment>
        <StatusTag
          colorCode={status}
          name={formatMessage({ id: status })}
        />
        {value}
      </Fragment>
    );
  }

  function renderActions({ record }) {
    const envId = record.get('id');
    const active = record.get('active');
    const synchronize = record.get('synchro');

    if (!synchronize && active) return null;

    const actionData = active ? [{
      text: formatMessage({ id: 'edit' }),
      action: () => openModifyModal(record),
    }, {
      text: formatMessage({ id: 'stop' }),
      // action: confirmDelete,
    }] : [{
      text: formatMessage({ id: 'active' }),
      // action: handleClick,
    }, {
      text: formatMessage({ id: 'delete' }),
      // action: confirmDelete,
    }];
    return <Action data={actionData} />;
  }

  return (
    <Fragment>
      <h2>{name}</h2>
      <Table
        dataSet={groupDs}
        border={false}
        queryBar="none"
      >
        <Column name="name" renderer={renderName} />
        <Column renderer={renderActions} width="0.7rem" />
        <Column name="description" />
        <Column name="clusterName" />
      </Table>
      <Modals />
    </Fragment>
  );
});

export default Group;
