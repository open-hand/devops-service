import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import uniqBy from 'lodash/uniqBy';
import HeaderButtons from '../../../components/header-buttons';
import EnvDetail from './env-detail';
import LinkService from './link-service';
import Permission from './permission';
import { useResourceStore } from '../../../../stores';
import { useEnvironmentStore } from '../stores';
import { useModalStore } from './stores';

const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalKey3 = Modal.key();

const EnvModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    resourceStore,
    treeDs,
  } = useResourceStore();
  const {
    envStore: {
      tabKey,
    },
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
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);

  const { menuId } = resourceStore.getSelectedMenu;

  function linkServices(data) {
    return modalStore.addService(projectId, menuId, data);
  }

  function addUsers(data) {
    const record = baseInfoDs.current;
    if (record) {
      const objectVersionNumber = record.get('objectVersionNumber');
      const users = {
        projectId,
        envId: menuId,
        objectVersionNumber,
        ...data,
      };
      return modalStore.addUsers(users);
    }

    return false;
  }

  function refresh() {
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
      children: <EnvDetail
        record={baseInfoDs.current}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        formatMessage={formatMessage}
      />,
      drawer: true,
      style: modalStyle,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
    });
  }

  function openLinkService() {
    modalStore.loadServices(projectId, menuId);
    Modal.open({
      key: modalKey2,
      title: formatMessage({ id: `${intlPrefix}.modal.link-service` }),
      style: modalStyle,
      drawer: true,
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
    modalStore.loadUsers(projectId, menuId);
    Modal.open({
      key: modalKey3,
      title: formatMessage({ id: `${intlPrefix}.modal.permission` }),
      drawer: true,
      style: modalStyle,
      children: <Permission
        store={modalStore}
        onOk={addUsers}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      afterClose: () => {
        modalStore.setUsers([]);
      },
    });
  }

  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.modal.link-service` }),
    icon: 'relate',
    handler: openLinkService,
    display: true,
    group: 1,
  }, {
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
    group: 1,
  }, {
    name: formatMessage({ id: 'refresh' }),
    icon: 'refresh',
    handler: refresh,
    display: true,
    group: 2,
  }]), []);

  return <HeaderButtons items={buttons} />;
});

export default EnvModals;
