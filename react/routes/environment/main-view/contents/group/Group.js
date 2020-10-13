import React, {
  Fragment, useMemo, useEffect, useState,
} from 'react';
import { observer } from 'mobx-react-lite';
import { Action, Choerodon } from '@choerodon/boot';
import {
  Modal, Table, Spin, Icon,
} from 'choerodon-ui/pro';
import { SagaDetails } from '@choerodon/master';
import checkPermission from '../../../../../utils/checkPermission';
import StatusTag from '../../../../../components/status-tag';
import eventStopProp from '../../../../../utils/eventStopProp';
import { getEnvStatus, statusMappings } from '../../../../../components/status-dot';
import ClickText from '../../../../../components/click-text';
import { handlePromptError } from '../../../../../utils';
import EnvModifyForm from '../../modals/env-modify';
import Modals from './modals';
import { useEnvironmentStore } from '../../../stores';
import { useMainStore } from '../../stores';
import { useEnvGroupStore } from './stores';

const { Column } = Table;
const envKey = Modal.key;
const modalKey = Modal.key;
const deleteKey = Modal.key;
const effectKey = Modal.key;
const formKey = Modal.key;

const Group = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    intlPrefix,
    envStore,
    treeDs,
    AppState: { currentMenuType: { id: projectId } },
    prefixCls,
  } = useEnvironmentStore();
  const { mainStore } = useMainStore();
  const {
    groupDs,
    intl: { formatMessage },
  } = useEnvGroupStore();
  const { getSelectedMenu: { name } } = envStore;

  const [canDetail, setCanDetail] = useState(false);

  useEffect(() => {
    async function init() {
      const res = await checkPermission({ projectId, code: 'choerodon.code.project.deploy.environment.ps.detail-group' });
      setCanDetail(res);
    }
    init();
  }, []);

  function refresh() {
    groupDs.query();
    treeDs.query();
  }

  async function openDelete(record) {
    const envId = record.get('id');
    const envName = record.get('name');

    const deleteModal = Modal.open({
      key: deleteKey,
      title: formatMessage({ id: `${intlPrefix}.delete.title` }, { name: envName }),
      children: <Spin />,
      footer: null,
      movable: false,
    });

    try {
      const res = await checkStatus(record);

      if (res) {
        const result = await mainStore.checkDelete(projectId, envId);
        if (result && result.failed) {
          deleteModal.close();
        } else if (result) {
          deleteModal.update({
            children: formatMessage({ id: `${intlPrefix}.delete.des.resource.confirm` }),
            okText: formatMessage({ id: 'delete' }),
            okProps: { color: 'red' },
            cancelProps: { color: 'dark' },
            onOk: handleDelete,
            footer: ((okBtn, cancelBtn) => (
              <>
                {cancelBtn}
                {okBtn}
              </>
            )),
          });
        } else {
          deleteModal.update({
            children: formatMessage({ id: `${intlPrefix}.delete.des.pipeline.confirm` }),
            okText: formatMessage({ id: 'iknow' }),
            footer: ((okBtn) => (
              <>
                {okBtn}
              </>
            )),
          });
        }
      } else {
        deleteModal.update({
          children: formatMessage({ id: `${intlPrefix}.status.change` }),
          onOk: refresh,
          footer: ((okBtn, cancelBtn) => (
            <>
              {okBtn}
            </>
          )),
        });
      }
    } catch (e) {
      Choerodon.handlePromptError(e);
      deleteModal.close();
    }
  }

  async function handleDelete() {
    const envId = groupDs.current ? groupDs.current.get('id') : null;
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

  function checkStatus(record) {
    const envId = record.get('id');
    const oldStatus = getStatusInRecord(record);
    return new Promise((resolve) => {
      mainStore.checkStatus(projectId, envId).then((res) => {
        if (res && res.id) {
          res.failed = res.fail;
          const newStatus = getEnvStatus(res);
          resolve(newStatus === oldStatus);
        }
      }).catch(() => resolve(false));
    });
  }

  async function openEffectModal(record) {
    const envId = record.get('id');
    const envName = record.get('name');
    const effectModal = Modal.open({
      key: effectKey,
      title: formatMessage({ id: `${intlPrefix}.stop.title` }, { name: envName }),
      children: <Spin />,
      footer: null,
      movable: false,
    });
    const res = await checkStatus(record);
    if (res) {
      try {
        const result = await mainStore.checkStop(projectId, envId);
        if (handlePromptError(result)) {
          effectModal.update({
            children: formatMessage({ id: `${intlPrefix}.stop.des` }),
            okText: formatMessage({ id: 'ok' }),
            okCancel: true,
            onOk: () => handleEffect(envId, false),
            footer: ((okBtn, cancelBtn) => (
              <>
                {okBtn}
                {cancelBtn}
              </>
            )),
          });
        } else if (!result.failed) {
          effectModal.update({
            children: formatMessage({ id: `${intlPrefix}.no.stop.des` }),
            okText: formatMessage({ id: 'iknow' }),
            footer: ((okBtn, cancelBtn) => (
              <>
                {okBtn}
              </>
            )),
          });
        } else {
          effectModal.close();
        }
      } catch (error) {
        Choerodon.handleResponseError(error);
        effectModal.close();
      }
    } else {
      effectModal.update({
        children: formatMessage({ id: `${intlPrefix}.status.change` }),
        onOk: refresh,
        footer: (okBtn, cancelBtn) => (
          <>
            {okBtn}
          </>
        ),
      });
    }
  }

  async function openModifyModal(record) {
    const modifyModal = Modal.open({
      key: formKey,
      title: formatMessage({ id: `${intlPrefix}.modify` }),
      style: modalStyle,
      children: <Spin />,
      drawer: true,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
    });
    try {
      const res = await checkStatus(record);
      if (res) {
        modifyModal.update({
          okCancel: true,
          children: <EnvModifyForm
            intlPrefix={intlPrefix}
            refresh={refresh}
            record={record}
            store={envStore}
          />,
          okText: formatMessage({ id: 'save' }),
        });
      } else {
        modifyModal.update({
          children: formatMessage({ id: `${intlPrefix}.status.change` }),
          okText: formatMessage({ id: 'iknow' }),
          onOk: refresh,
        });
      }
    } catch (error) {
      Choerodon.handlePromptError(error);
      modifyModal.close();
    }
  }

  function getStatusInRecord(record) {
    const active = record.get('active');
    const connect = record.get('connect');
    const failed = record.get('failed');
    const synchronize = record.get('synchro');
    return getEnvStatus({
      active,
      connect,
      failed,
      synchronize,
    });
  }

  function openSagaDetails(id) {
    Modal.open({
      title: formatMessage({ id: 'global.saga-instance.detail' }),
      key: Modal.key(),
      children: <SagaDetails sagaInstanceId={id} instance />,
      drawer: true,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
      style: {
        width: 'calc(100% - 3.5rem)',
      },
    });
  }

  function renderName({ value, record }) {
    const { RUNNING, DISCONNECTED } = statusMappings;
    const status = getStatusInRecord(record);
    return (
      <>
        <StatusTag
          colorCode={status}
          name={formatMessage({ id: status })}
        />
        <ClickText
          value={value}
          clickAble={(status === RUNNING || status === DISCONNECTED) && canDetail}
          onClick={() => openModifyModal(record)}
          record={record}
        />
        {record.get('sagaInstanceId') ? (
          <Icon
            className={`${prefixCls}-dashBoard`}
            type="developer_board"
            onClick={() => openSagaDetails(record.get('sagaInstanceId'))}
          />
        ) : ''}
      </>
    );
  }

  function renderActions({ record }) {
    const {
      RUNNING, DISCONNECTED, FAILED, OPERATING, STOPPED,
    } = statusMappings;
    const status = getStatusInRecord(record);
    const envId = record.get('id');
    const hasSagaInstanceId = record.get('sagaInstanceId');
    if (status === OPERATING) return null;

    let actionData = [];

    if (hasSagaInstanceId) {
      actionData = [{
        service: ['choerodon.code.project.deploy.environment.ps.delete'],
        text: formatMessage({ id: `${intlPrefix}.modal.detail.delete` }),
        action: () => openDelete(record),
      }];
    } else {
      switch (status) {
        case RUNNING:
          actionData = [{
            service: ['choerodon.code.project.deploy.environment.ps.modify'],
            text: formatMessage({ id: `${intlPrefix}.modal.detail.modify` }),
            action: openModifyModal.bind(this, record),
          }, {
            service: ['choerodon.code.project.deploy.environment.ps.stop'],
            text: formatMessage({ id: `${intlPrefix}.modal.detail.stop` }),
            action: () => openEffectModal(record),
          }];
          break;
        case DISCONNECTED:
          actionData = [{
            service: ['choerodon.code.project.deploy.environment.ps.modify'],
            text: formatMessage({ id: `${intlPrefix}.modal.detail.modify` }),
            action: () => openModifyModal(record),
          }, {
            service: ['choerodon.code.project.deploy.environment.ps.delete'],
            text: formatMessage({ id: `${intlPrefix}.modal.detail.delete` }),
            action: () => openDelete(record),
          }];
          break;
        case STOPPED:
          actionData = [{
            service: ['choerodon.code.project.deploy.environment.ps.stop'],
            text: formatMessage({ id: `${intlPrefix}.modal.detail.start` }),
            action: () => handleEffect(envId, true),
          }, {
            service: ['choerodon.code.project.deploy.environment.ps.delete'],
            text: formatMessage({ id: `${intlPrefix}.modal.detail.delete` }),
            action: () => openDelete(record),
          }];
          break;
        case FAILED:
          actionData = [{
            service: ['choerodon.code.project.deploy.environment.ps.delete'],
            text: formatMessage({ id: `${intlPrefix}.modal.detail.delete` }),
            action: () => openDelete(record),
          }];
          break;
        default:
      }
    }

    return (
      <Action
        placement="bottomRight"
        data={actionData}
        onClick={eventStopProp}
      />
    );
  }

  return (
    <>
      <h2>{name}</h2>
      <Table
        dataSet={groupDs}
        border={false}
        queryBar="none"
      >
        <Column name="name" renderer={renderName} />
        <Column renderer={renderActions} width={100} />
        <Column name="description" />
        <Column name="clusterName" />
      </Table>
      <Modals />
    </>
  );
});

export default Group;
