import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import EnvDetail from '../../../../../../components/env-detail';
import LinkService from './link-service';
import PermissionPage from './permission';
import { useResourceStore } from '../../../../stores';
import { useEnvironmentStore } from '../stores';
import { useModalStore } from './stores';

import '../../../../../../components/dynamic-select/style';

const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalKey3 = Modal.key();

const EnvModals = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    resourceStore: { getSelectedMenu: { id } },
    AppState: { currentMenuType: { id: projectId } },
    treeDs,
  } = useResourceStore();
  const {
    envStore,
    tabs: {
      SYNC_TAB,
      ASSIGN_TAB,
    },
    permissionsDs,
    gitopsLogDs,
    gitopsSyncDs,
    baseInfoDs,
  } = useEnvironmentStore();
  const {
    modalStore,
  } = useModalStore();

  function linkServices(data) {
    return modalStore.addService(projectId, id, data);
  }

  function addUsers(data) {
    const record = baseInfoDs.current;
    if (record) {
      const objectVersionNumber = record.get('objectVersionNumber');
      const users = {
        projectId,
        envId: id,
        objectVersionNumber,
        ...data,
      };
      return modalStore.addUsers(users);
    }

    return false;
  }

  function refresh() {
    baseInfoDs.query();
    treeDs.query();
    const tabKey = envStore.getTabKey;
    if (tabKey === SYNC_TAB) {
      gitopsSyncDs.query();
      gitopsLogDs.query();
    } else if (tabKey === ASSIGN_TAB) {
      permissionsDs.query();
    }
  }

  function openEnvDetail() {
    Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
      children: <EnvDetail record={baseInfoDs.current} />,
      drawer: true,
      style: modalStyle,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
    });
  }

  function openLinkService() {
    modalStore.loadServices(projectId, id);
    Modal.open({
      key: modalKey2,
      title: formatMessage({ id: `${intlPrefix}.modal.service.link` }),
      style: modalStyle,
      drawer: true,
      className: 'c7ncd-modal-wrapper',
      children: <LinkService
        store={modalStore}
        tree={treeDs}
        onOk={linkServices}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      afterClose: () => {
        modalStore.setServices([]);
      },
    });
  }

  function openPermission() {
    modalStore.loadUsers(projectId, id);
    Modal.open({
      key: modalKey3,
      title: formatMessage({ id: `${intlPrefix}.modal.permission` }),
      drawer: true,
      style: modalStyle,
      className: 'c7ncd-modal-wrapper',
      children: <PermissionPage
        store={modalStore}
        onOk={addUsers}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        skipPermission={baseInfoDs.current.get('skipCheckPermission')}
        refresh={toPermissionTab}
      />,
      afterClose: () => {
        modalStore.setUsers([]);
      },
    });
  }

  function toPermissionTab() {
    const { getTabKey } = envStore;
    envStore.setTabKey(ASSIGN_TAB);
    baseInfoDs.query();
    getTabKey === ASSIGN_TAB && permissionsDs.query();
  }

  function getButtons() {
    const { getTabKey } = envStore;
    let isSync;
    const record = baseInfoDs.current;
    if (record) {
      isSync = record.get('synchronize');
    }
    return [{
      name: formatMessage({ id: `${intlPrefix}.modal.service.link` }),
      icon: 'relate',
      handler: openLinkService,
      display: true,
      disabled: !isSync,
      group: 1,
    }, {
      permissions: ['devops-service.devops-environment.pageEnvUserPermissions'],
      name: formatMessage({ id: `${intlPrefix}.modal.permission` }),
      icon: 'authority',
      handler: openPermission,
      display: getTabKey === ASSIGN_TAB,
      disabled: !isSync,
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

  return <HeaderButtons items={getButtons()} />;
});

export default EnvModals;
