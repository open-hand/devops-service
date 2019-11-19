import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action, Choerodon } from '@choerodon/boot';
import { Modal, Spin } from 'choerodon-ui/pro';
import { handlePromptError } from '../../../../../utils';
import eventStopProp from '../../../../../utils/eventStopProp';
import TreeItemName from '../../../../../components/treeitem-name';
import EnvItem from '../../../../../components/env-item';
import { statusMappings, getEnvStatus } from '../../../../../components/status-dot';
import EnvModifyForm from '../../modals/env-modify';
import { useEnvironmentStore } from '../../../stores';
import { useMainStore } from '../../stores';

const formKey = Modal.key();
const effectKey = Modal.key();
const deleteKey = Modal.key();

function DetailItem({ record, search, intl: { formatMessage }, intlPrefix }) {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    treeDs,
    AppState: { currentMenuType: { id: projectId } },
    envStore,
  } = useEnvironmentStore();
  const { mainStore } = useMainStore();

  function refresh() {
    treeDs.query();
  }

  async function handleDelete() {
    const envId = record.get('id');
    try {
      const res = await mainStore.deleteEnv(projectId, envId);
      handlePromptError(res);
    } catch (e) {
      Choerodon.handleResponseError(e);
    } finally {
      refresh();
    }
  }

  async function openDelete() {
    const name = record.get('name');
    const deleteModal = Modal.open({
      key: deleteKey,
      title: formatMessage({ id: `${intlPrefix}.delete.title` }, { name }),
      children: <Spin />,
      okCancel: false,
      okText: formatMessage({ id: 'iknow' }),
    });
    const res = await checkStatus();
    if (res) {
      deleteModal.update({
        children: formatMessage({ id: `${intlPrefix}.delete.des` }),
        okText: formatMessage({ id: 'delete' }),
        okProps: { color: 'red' },
        cancelProps: { color: 'dark' },
        okCancel: true,
        onOk: handleDelete,
      });
    } else {
      deleteModal.update({
        children: formatMessage({ id: `${intlPrefix}.status.change` }),
        onOk: refresh,
      });
    }
  }

  function checkStatus() {
    const envId = record.get('id');
    const status = getStatusInRecord();
    const oldStatus = getEnvStatus(status);
    return new Promise((resolve) => {
      mainStore.checkStatus(projectId, envId).then((res) => {
        if (handlePromptError(res)) {
          const newStatus = getEnvStatus(res);
          resolve(newStatus === oldStatus);
        }
      });
    });
  }

  async function openModifyModal() {
    const modifyModal = Modal.open({
      key: formKey,
      title: formatMessage({ id: `${intlPrefix}.modify` }),
      style: modalStyle,
      children: <Spin />,
      drawer: true,
      okCancel: false,
      okText: formatMessage({ id: 'iknow' }),
    });
    const res = await checkStatus();
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
        onOk: refresh,
      });
    }
  }

  async function handleEffect(target) {
    try {
      const envId = record.get('id');
      const res = await mainStore.effectEnv(projectId, envId, target);
      if (handlePromptError(res)) {
        envStore.setUpTarget(envId);
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
    } finally {
      refresh();
    }
  }

  async function openEffectModal() {
    const envId = record.get('id');
    const name = record.get('name');
    const effectModal = Modal.open({
      key: effectKey,
      title: formatMessage({ id: `${intlPrefix}.stop.title` }, { name }),
      children: <Spin />,
      okCancel: false,
      okText: formatMessage({ id: 'iknow' }),
    });
    const res = await checkStatus();
    if (res) {
      const result = await mainStore.checkEffect(projectId, envId);
      if (handlePromptError(result)) {
        effectModal.update({
          children: formatMessage({ id: `${intlPrefix}.stop.des` }),
          okText: formatMessage({ id: 'ok' }),
          okCancel: true,
          onOk: () => handleEffect(false),
        });
      } else {
        effectModal.update({
          children: formatMessage({ id: `${intlPrefix}.no.stop.des` }),
        });
      }
    } else {
      effectModal.update({
        children: formatMessage({ id: `${intlPrefix}.status.change` }),
        onOk: refresh,
      });
    }
  }

  function getStatusInRecord() {
    const connect = record.get('connect');
    const failed = record.get('failed');
    const synchronize = record.get('synchro');
    const active = record.get('active');
    return {
      connect,
      failed,
      synchronize,
      active,
    };
  }

  function getSuffix() {
    const { RUNNING, STOPPED, OPERATING, FAILED, DISCONNECTED } = statusMappings;
    const status = getStatusInRecord();
    const result = getEnvStatus(status);

    if (result === OPERATING) return null;

    let actionData = [];

    switch (result) {
      case RUNNING:
        actionData = [{
          service: [],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.stop` }),
          action: openEffectModal,
        }, {
          service: [],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.modify` }),
          action: openModifyModal,
        }];
        break;
      case DISCONNECTED:
        actionData = [{
          service: [],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.modify` }),
          action: openModifyModal,
        }, {
          service: [],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.delete` }),
          action: openDelete,
        }];
        break;
      case STOPPED:
        actionData = [{
          service: [],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.start` }),
          action: () => handleEffect(true),
        }, {
          service: [],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.delete` }),
          action: openDelete,
        }];
        break;
      case FAILED:
        actionData = [{
          service: [],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.delete` }),
          action: openDelete,
        }];
        break;
      default:
    }

    return <Action
      placement="bottomRight"
      data={actionData}
      onClick={eventStopProp}
    />;
  }

  function getName() {
    const { OPERATING, FAILED } = statusMappings;
    const itemName = record.get('name') || '';
    const status = getStatusInRecord();
    const result = getEnvStatus(status);
    const disabled = result === OPERATING || result === FAILED;
    const name = <TreeItemName
      disabled={disabled}
      name={itemName}
      search={search}
    />;
    return <EnvItem
      name={name}
      {...status}
    />;
  }

  return <Fragment>
    {getName()}
    {getSuffix()}
  </Fragment>;
}

DetailItem.propTypes = {
  search: PropTypes.string,
};

export default injectIntl(observer(DetailItem));
