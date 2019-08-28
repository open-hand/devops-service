import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import EnvDetail from '../../../../../../components/env-detail';
import Permission from '../../../../../resource/main-view/contents/environment/modals/permission';
import { useEnvironmentStore } from '../../../../stores';
import { useMainStore } from '../../../stores';
import { useDetailStore } from '../stores';
import EnvCreateForm from '../../../modals/EnvCreateForm';
import GroupCreateForm from '../../../modals/GroupCreateForm';
import useStore from './useStore';

import './index.less';

const detailKey = Modal.key();
const envKey = Modal.key();
const groupKey = Modal.key();
const permissionKey = Modal.key();

const EnvModals = observer(() => {
  const modalStore = useStore();
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    treeDs,
    intlPrefix: currentIntlPrefix,
    prefixCls: currentPrefixCls,
    envStore: { getSelectedMenu },
    AppState: { currentMenuType: { id: projectId } },
  } = useEnvironmentStore();
  const {
    envFormDs,
    groupFormDs,
    clusterDs,
  } = useMainStore();
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    detailStore: {
      getTabKey,
    },
    tabs: {
      SYNC_TAB,
      CONFIG_TAB,
      ASSIGN_TAB,
    },
    permissionsDs,
    gitopsLogDs,
    gitopsSyncDs,
    configDs,
  } = useDetailStore();

  function refresh() {
    treeDs.query();
    switch (getTabKey) {
      case SYNC_TAB: {
        gitopsSyncDs.query();
        gitopsLogDs.query();
        break;
      }
      case CONFIG_TAB:
        configDs.query();
        break;
      case ASSIGN_TAB:
        permissionsDs.query();
        break;
      default:
    }
  }

  function openEnvModal() {
    clusterDs.query();
    Modal.open({
      key: envKey,
      title: formatMessage({ id: `${currentIntlPrefix}.create` }),
      children: <EnvCreateForm dataSet={envFormDs} clusterDs={clusterDs} />,
      drawer: true,
      style: modalStyle,
    });
  }

  function openGroupModal() {
    groupFormDs.reset();
    Modal.open({
      key: groupKey,
      title: formatMessage({ id: `${currentIntlPrefix}.group.create` }),
      children: <GroupCreateForm dataSet={groupFormDs} treeDs={treeDs} />,
      drawer: true,
      style: modalStyle,
    });
  }

  function openEnvDetail() {
    Modal.open({
      key: detailKey,
      title: formatMessage({ id: `${currentIntlPrefix}.modal.env-detail` }),
      children: <EnvDetail record={getSelectedMenu} isRecord={false} />,
      drawer: true,
      style: modalStyle,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
    });
  }

  async function addUsers(data) {
    const { id, objectVersionNumber } = getSelectedMenu;
    const users = {
      projectId,
      envId: id,
      objectVersionNumber,
      ...data,
    };
    return modalStore.addUsers(users);
  }

  function openPermission() {
    const { id, skipCheckPermission } = getSelectedMenu;
    modalStore.loadUsers(projectId, id);
    Modal.open({
      key: permissionKey,
      title: formatMessage({ id: `${intlPrefix}.modal.permission` }),
      drawer: true,
      style: modalStyle,
      children: <Permission
        store={modalStore}
        onOk={addUsers}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        skipPermission={skipCheckPermission}
        refresh={refresh}
      />,
      afterClose: () => {
        modalStore.setUsers([]);
      },
    });
  }

  function getButtons() {
    const { active, synchro } = getSelectedMenu;
    const disabled = !active || !synchro;
    return [{
      name: formatMessage({ id: `${currentIntlPrefix}.create` }),
      icon: 'playlist_add',
      handler: openEnvModal,
      display: true,
      group: 1,
    }, {
      disabled,
      name: formatMessage({ id: `${currentIntlPrefix}.create.config` }),
      icon: 'playlist_add',
      handler: openPermission,
      display: true,
      group: 1,
    }, {
      disabled,
      name: formatMessage({ id: `${intlPrefix}.modal.permission` }),
      icon: 'authority',
      handler: openPermission,
      display: true,
      group: 1,
    }, {
      name: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
      icon: 'find_in_page',
      handler: openEnvDetail,
      display: true,
      group: 2,
    }, {
      name: formatMessage({ id: 'refresh' }),
      icon: 'refresh',
      handler: refresh,
      display: true,
      group: 2,
    }];
  }

  function getOtherBtn() {
    const actionData = [{
      text: formatMessage({ id: `${currentIntlPrefix}.group.create` }),
      action: openGroupModal,
    },
    {
      text: formatMessage({ id: `${currentIntlPrefix}.resource.setting` }),
      action: openGroupModal,
    }];
    return <Action data={actionData} />;
  }

  return <HeaderButtons items={getButtons()}>
    <div className={`${currentPrefixCls}-other-btn`}>{getOtherBtn()}</div>
  </HeaderButtons>;
});

export default EnvModals;
