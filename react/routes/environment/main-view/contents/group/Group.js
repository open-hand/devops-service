import React, { Fragment, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import { Modal, Table } from 'choerodon-ui/pro';
import StatusTag from '../../../../../components/status-tag';
import { getEnvStatus } from '../../../../../components/status-dot';
import { useEnvironmentStore } from '../../../stores';
import { useMainStore } from '../../stores';
import { useEnvGroupStore } from './stores';
import Modals from './modals';
import EnvModifyForm from '../../modals/env-modify';
import { handlePromptError } from '../../../../../utils';

const { Column } = Table;
const envKey = Modal.key;
const modalKey = Modal.key;

const Group = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    intlPrefix,
    envStore,
    treeDs,
    AppState: { currentMenuType: { id: projectId } },
  } = useEnvironmentStore();
  const { mainStore } = useMainStore();
  const {
    groupDs,
    intl: { formatMessage },
  } = useEnvGroupStore();
  const { getSelectedMenu: { name } } = envStore;

  function refresh() {
    groupDs.query();
    treeDs.query();
  }

  async function handleDelete(envId) {
    try {
      const res = await mainStore.deleteEnv(projectId, envId);
      handlePromptError(res);
    } catch (e) {
      Choerodon.handleResponseError(e);
    } finally {
      refresh();
    }
  }

  async function handleEffect(envId, target) {
    try {
      const res = await mainStore.effectEnv(projectId, envId, target);
      handlePromptError(res);
    } catch (e) {
      Choerodon.handleResponseError(e);
    } finally {
      refresh();
    }
  }

  async function openEffectModal(envId) {
    let children;
    let title;
    let disabled = true;
    try {
      const res = await mainStore.checkEffect(projectId, envId);
      if (handlePromptError(res)) {
        title = '确认停用';
        children = '当你点击确认后，该环境将被停用！';
        disabled = false;
      } else {
        title = '不可停用';
        children = '该环境下已有实例，且此环境正在运行中，无法停用！';
      }
    } catch (e) {
      title = '出错了';
      children = '请稍后重试。';
      Choerodon.handleResponseError(e);
    }
    Modal.open({
      movable: false,
      closable: false,
      header: true,
      key: modalKey,
      title,
      children,
      onOk: () => handleEffect(envId, false),
      okProps: {
        disabled,
      },
    });
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
      service: [],
      text: formatMessage({ id: 'edit' }),
      action: () => openModifyModal(record),
    }, {
      service: [],
      text: formatMessage({ id: 'stop' }),
      action: () => openEffectModal(envId),
    }] : [{
      service: [],
      text: formatMessage({ id: 'active' }),
      action: () => handleEffect(envId, true),
    }, {
      service: [],
      text: formatMessage({ id: 'delete' }),
      action: () => handleDelete(envId),
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
        <Column renderer={renderActions} width={70} />
        <Column name="description" />
        <Column name="clusterName" />
      </Table>
      <Modals />
    </Fragment>
  );
});

export default Group;
