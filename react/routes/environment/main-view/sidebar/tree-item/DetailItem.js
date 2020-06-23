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
    itemType: {
      GROUP_ITEM,
    },
  } = useEnvironmentStore();
  const { mainStore } = useMainStore();
  const { getSelectedMenu: { itemType, key } } = envStore;

  function refresh() {
    treeDs.query();
    if (itemType === GROUP_ITEM && key === record.get('parentId')) {
      envStore.setUpTarget(key);
    }
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
    const envId = record.get('id');

    const deleteModal = Modal.open({
      key: deleteKey,
      title: formatMessage({ id: `${intlPrefix}.delete.title` }, { name }),
      children: <Spin />,
      footer: null,
      movable: false,
    });
    try {
      const res = await checkStatus();

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
              <Fragment>
                {cancelBtn}{okBtn}
              </Fragment>
            )),
          });
        } else {
          deleteModal.update({
            children: formatMessage({ id: `${intlPrefix}.delete.des.pipeline.confirm` }),
            okText: formatMessage({ id: 'iknow' }),
            footer: ((okBtn) => (
              <Fragment>
                {okBtn}
              </Fragment>
            )),
          });
        }
      } else {
        deleteModal.update({
          children: formatMessage({ id: `${intlPrefix}.status.change` }),
          okText: formatMessage({ id: 'iknow' }),
          onOk: refresh,
          footer: ((okBtn) => (
            <Fragment>
              {okBtn}
            </Fragment>
          )),
        });
      }
    } catch (e) {
      Choerodon.handlePromptError(e);
      deleteModal.close();
    }
  }

  function checkStatus() {
    const envId = record.get('id');
    const status = getStatusInRecord();
    const oldStatus = getEnvStatus(status);
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

  async function openModifyModal() {
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
          okText: formatMessage({ id: 'iknow' }),
          onOk: refresh,
        });
      }
    } catch (error) {
      Choerodon.handlePromptError(error);
      modifyModal.close();
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
      footer: null,
      movable: false,
    });
    const res = await checkStatus();
    if (res) {
      try {
        const result = await mainStore.checkStop(projectId, envId);
        if (handlePromptError(result)) {
          effectModal.update({
            children: formatMessage({ id: `${intlPrefix}.stop.des` }),
            okText: formatMessage({ id: 'ok' }),
            okCancel: true,
            onOk: () => handleEffect(false),
            footer: ((okBtn, cancelBtn) => (
              <Fragment>
                {okBtn}{cancelBtn}
              </Fragment>
            )),
          });
        } else if (!result.failed) {
          effectModal.update({
            children: formatMessage({ id: `${intlPrefix}.no.stop.des` }),
            okText: formatMessage({ id: 'iknow' }),
            footer: ((okBtn, cancelBtn) => (
              <Fragment>
                {okBtn}
              </Fragment>
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
          <Fragment>
            {okBtn}
          </Fragment>
        ),
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
          service: ['choerodon.code.project.deploy.environment.ps.modify'],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.modify` }),
          action: openModifyModal,
        }, {
          service: ['choerodon.code.project.deploy.environment.ps.stop'],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.stop` }),
          action: openEffectModal,
        }];
        break;
      case DISCONNECTED:
        actionData = [{
          service: ['choerodon.code.project.deploy.environment.ps.modify'],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.modify` }),
          action: openModifyModal,
        }, {
          service: ['choerodon.code.project.deploy.environment.ps.delete'],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.delete` }),
          action: openDelete,
        }];
        break;
      case STOPPED:
        actionData = [{
          service: ['choerodon.code.project.deploy.environment.ps.stop'],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.start` }),
          action: () => handleEffect(true),
        }, {
          service: ['choerodon.code.project.deploy.environment.ps.delete'],
          text: formatMessage({ id: `${intlPrefix}.modal.detail.delete` }),
          action: openDelete,
        }];
        break;
      case FAILED:
        actionData = [{
          service: ['choerodon.code.project.deploy.environment.ps.delete'],
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
