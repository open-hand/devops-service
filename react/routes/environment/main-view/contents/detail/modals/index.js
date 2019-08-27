import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import EnvDetail from '../../../../../../components/env-detail';
// import LinkService from './link-service';
// import Permission from './permission';
import { useEnvironmentStore } from '../../../../stores';
import { useDetailStore } from '../stores';
// import { useModalStore } from './stores';

const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalKey3 = Modal.key();

const EnvModals = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    treeDs,
    envStore: { getSelectedMenu },
  } = useEnvironmentStore();
  const {
    intl: { formatMessage },
    currentIntlPrefix,
    intlPrefix,
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

  function openEnvDetail() {
    Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
      children: <EnvDetail record={getSelectedMenu} isRecord={false} />,
      drawer: true,
      style: modalStyle,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
    });
  }

  function openLinkService() {
    // modalStore.loadServices(projectId, menuId);
    // Modal.open({
    //   key: modalKey2,
    //   title: formatMessage({ id: `${intlPrefix}.modal.link-service` }),
    //   style: modalStyle,
    //   drawer: true,
    //   children: <LinkService
    //     store={modalStore}
    //     tree={treeDs}
    //     onOk={linkServices}
    //     intlPrefix={intlPrefix}
    //     prefixCls={prefixCls}
    //   />,
    //   afterClose: () => {
    //     modalStore.setServices([]);
    //   },
    // });
  }

  function openPermission() {
    // modalStore.loadUsers(projectId, menuId);
    // Modal.open({
    //   key: modalKey3,
    //   title: formatMessage({ id: `${intlPrefix}.modal.permission` }),
    //   drawer: true,
    //   style: modalStyle,
    //   children: <Permission
    //     store={modalStore}
    //     onOk={addUsers}
    //     intlPrefix={intlPrefix}
    //     prefixCls={prefixCls}
    //   />,
    //   afterClose: () => {
    //     modalStore.setUsers([]);
    //   },
    // });
  }

  function getButtons() {
    const { active, synchro } = getSelectedMenu;
    const disabled = !active || !synchro;
    return [{
      name: formatMessage({ id: `${currentIntlPrefix}.create` }),
      icon: 'playlist_add',
      handler: openLinkService,
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

  return <HeaderButtons items={getButtons()} />;
});

export default EnvModals;
