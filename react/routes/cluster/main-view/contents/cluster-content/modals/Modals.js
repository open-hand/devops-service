import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useClusterContentStore } from '../stores';
import { useClusterStore } from '../../../../stores';
import { useClusterMainStore } from '../../../stores';
import { useModalStore } from './stores';
import CreateCluster from './create-cluster';
import PermissionManage from './permission-manage';
import Tips from '../../../../../../components/new-tips';

import '../../../../../../components/dynamic-select/style/index.less';

const modalKey1 = Modal.key();
const modalKey2 = Modal.key();

const ClusterModals = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    clusterStore: {
      getSelectedMenu: { id },
    },
    AppState: { currentMenuType: { id: projectId } },
    treeDs,
  } = useClusterStore();
  const {
    contentStore,
    tabs: { NODE_TAB, ASSIGN_TAB },
    PermissionDs,
    NodeListDs,
    ClusterDetailDs,
  } = useClusterContentStore();
  const { getTabKey } = contentStore;

  const { mainStore } = useClusterMainStore();
  const {
    modalStore,
    NonPermissionDs,
  } = useModalStore();

  function permissionUpdate(data) {
    const record = ClusterDetailDs.current;
    if (record) {
      const objectVersionNumber = record.get('objectVersionNumber');
      const PermissionData = {
        projectId,
        clusterId: id,
        objectVersionNumber,
        ...data,
      };
      return modalStore.permissionUpdate(PermissionData);
    }
    return false;
  }

  function refresh() {
    resreshTree();
    ClusterDetailDs.query();
    if (getTabKey === NODE_TAB) {
      NodeListDs.query();
    } else {
      PermissionDs.query();
    }
  }
  function resreshTree() {
    treeDs.query();
  }

  function refreshPermission() {
    contentStore.setTabKey(ASSIGN_TAB);
    ClusterDetailDs.query();
    NonPermissionDs.query();
  }

  function openCreate() {
    Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.modal.create` }),
      children: <CreateCluster afterOk={resreshTree} prefixCls={prefixCls} intlPrefix={intlPrefix} formatMessage={formatMessage} mainStore={mainStore} projectId={projectId} />,
      drawer: true,
      style: modalStyle,
      okText: formatMessage({ id: 'create' }),
    });
  }

  function openPermission() {
    const arr = NonPermissionDs.toData();
    Modal.open({
      key: modalKey2,
      title: <Tips
        helpText={formatMessage({ id: `${intlPrefix}.permission.tips` })}
        title={formatMessage({ id: `${intlPrefix}.modal.permission` })}
      />,
      drawer: true,
      style: modalStyle,
      className: 'c7ncd-modal-wrapper',
      children: <PermissionManage
        refreshPermission={refreshPermission}
        projectList={arr}
        onOk={permissionUpdate}
        clusterDetail={ClusterDetailDs.current}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        formatMessage={formatMessage}
      />,
    });
  }

  function getButtons() {
    return [{
      name: formatMessage({ id: `${intlPrefix}.modal.create` }),
      permissions: ['devops-service.devops-cluster.create'],
      icon: 'playlist_add',
      handler: openCreate,
      display: true,
      group: 1,
    }, {
      name: formatMessage({ id: `${intlPrefix}.modal.permission` }),
      permissions: ['devops-service.devops-cluster.assignPermission', 'devops-service.devops-cluster.checkName',
        'devops-service.devops-cluster.checkCode'],
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
