import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action, Choerodon } from '@choerodon/boot';
import { Modal } from 'choerodon-ui/pro';
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

  function openDelete() {
    const name = record.get('name');
    Modal.open({
      key: deleteKey,
      title: formatMessage({ id: `${intlPrefix}.delete.title` }, { name }),
      children: formatMessage({ id: `${intlPrefix}.delete.des` }),
      okText: formatMessage({ id: 'delete' }),
      okProps: { color: 'red' },
      cancelProps: { color: 'dark' },
      onOk: handleDelete,
    });
  }

  function openModifyModal() {
    Modal.open({
      key: formKey,
      title: formatMessage({ id: `${intlPrefix}.modify` }),
      children: <EnvModifyForm
        intlPrefix={intlPrefix}
        refresh={refresh}
        record={record}
        store={envStore}
      />,
      drawer: true,
      style: modalStyle,
      okText: formatMessage({ id: 'save' }),
    });
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
    let children;
    let title;
    let disabled = true;
    const envId = record.get('id');
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
      key: effectKey,
      title,
      children,
      onOk: () => handleEffect(false),
      okProps: {
        disabled,
      },
    });
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
      case DISCONNECTED:
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
