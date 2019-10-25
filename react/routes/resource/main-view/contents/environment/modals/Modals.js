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
import Tips from '../../../../../../components/new-tips';
import DeployConfigForm from './deploy-config';

import '../../../../../../components/dynamic-select/style/index.less';

const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalKey3 = Modal.key();
const configKey = Modal.key();

const EnvModals = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const configModalStyle = useMemo(() => ({
    width: 'calc(100vw - 3.52rem)',
    minWidth: '2rem',
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
    configFormDs,
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
    const record = baseInfoDs.current;
    const data = record ? {
      ...record.toData(),
      synchronize: true,
    } : null;
    Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
      children: <EnvDetail record={data} isRecord={false} />,
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
      title: <Tips
        helpText={formatMessage({ id: `${intlPrefix}.service.tips` })}
        title={formatMessage({ id: `${intlPrefix}.modal.service.link` })}
      />,
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
      title: <Tips
        helpText={formatMessage({ id: `${intlPrefix}.permission.tips` })}
        title={formatMessage({ id: `${intlPrefix}.modal.permission` })}
      />,
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
    baseInfoDs.query();
    envStore.setTabKey(ASSIGN_TAB);
    getTabKey === ASSIGN_TAB && permissionsDs.query();
  }
  
  function linkToConfig() {
    const record = baseInfoDs.current;
    const url = record && record.get('gitlabUrl');
    url && window.open(url);
  }

  function openConfigModal() {
    configFormDs.create();
    Modal.open({
      key: configKey,
      title: formatMessage({ id: `${intlPrefix}.create.config` }),
      children: <DeployConfigForm
        store={envStore}
        dataSet={configFormDs}
        refresh={refresh}
        envId={id}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      drawer: true,
      style: configModalStyle,
      afterClose: () => {
        configFormDs.reset();
        envStore.setValue('');
      },
      okText: formatMessage({ id: 'create' }),
    });
  }

  function getButtons() {
    const record = baseInfoDs.current;
    const notReady = !record;
    const connect = record && record.get('connect');
    const configDisabled = !connect || notReady;
    return [{
      name: formatMessage({ id: `${intlPrefix}.modal.service.link` }),
      icon: 'relate',
      handler: openLinkService,
      display: true,
      disabled: notReady,
      group: 1,
    }, {
      disabled: configDisabled,
      name: formatMessage({ id: `${intlPrefix}.create.config` }),
      icon: 'playlist_add',
      handler: openConfigModal,
      display: true,
      group: 1,
    }, {
      permissions: ['devops-service.devops-environment.pageEnvUserPermissions'],
      name: formatMessage({ id: `${intlPrefix}.modal.permission` }),
      icon: 'authority',
      handler: openPermission,
      display: true,
      disabled: notReady,
      group: 1,
    }, {
      disabled: notReady,
      name: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
      icon: 'find_in_page',
      handler: openEnvDetail,
      display: true,
      group: 2,
    }, {
      disabled: notReady,
      name: formatMessage({ id: `${intlPrefix}.environment.config-lab` }),
      icon: 'account_balance',
      handler: linkToConfig,
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
