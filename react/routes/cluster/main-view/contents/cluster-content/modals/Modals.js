import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useClusterContentStore } from '../stores';
import { useClusterStore } from '../../../../stores';
import { useModalStore } from './stores';
import CreateCluster from './create-cluster';
import PermissionManage from './permission-manage';

const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalKey3 = Modal.key();

const ClusterModals = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    clusterStore,
    AppState: { currentMenuType: { id: projectId } },
    treeDs,
  } = useClusterStore();
  const {
    contentStore: {
      getTabKey,
    },
    tabs: {
      NODE_TAB,
      ASSIGN_TAB,
    },
    PermissionDs,
    NodeListDs,
    ClusterDetailDs,
  } = useClusterContentStore();
  const {
    modalStore,
    NonPermissionDs,
  } = useModalStore();

  const { menuId } = clusterStore.getSelectedMenu;

  function linkServices(data) {

  }

  function permissionUpdate(data) {
    const record = ClusterDetailDs.current;
    if (record) {
      const objectVersionNumber = record.get('objectVersionNumber');
      const PermissionData = {
        projectId,
        clusterId: menuId,
        objectVersionNumber,
        ...data,
      };
      return modalStore.permissionUpdate(PermissionData);
    }
    return false;
  }

  function refresh() {
    if (getTabKey === NODE_TAB) {
      NodeListDs.query();
    } else {
      PermissionDs.query();
    }
  }

  function openCreate() {
    Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.modal.create` }),
      children: <CreateCluster prefixCls={prefixCls} intlPrefix={intlPrefix} formatMessage={formatMessage} />,
      drawer: true,
      style: modalStyle,
      okText: formatMessage({ id: 'save' }),
    });
  }

  function openPermission() {
    const arr = NonPermissionDs.toData();
    Modal.open({
      key: modalKey2,
      title: formatMessage({ id: `${intlPrefix}.modal.permission` }),
      drawer: true,
      style: modalStyle,
      children: <PermissionManage
        projectList={arr}
        onOk={permissionUpdate}
        clusterDetail={ClusterDetailDs.current}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        formatMessage={formatMessage}
      />,
      afterClose: () => {
      },
    });
  }

  function getButtons() {
    return [{
      name: formatMessage({ id: `${intlPrefix}.modal.create` }),
      icon: 'playlist_add',
      handler: openCreate,
      display: true,
      group: 1,
    }, {
      name: formatMessage({ id: `${intlPrefix}.modal.permission` }),
      icon: 'authority',
      handler: openPermission,
      display: true,
      group: 1,
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

export default ClusterModals;
