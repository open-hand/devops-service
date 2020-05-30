import React, { useMemo, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useResourceStore } from '../../../../stores';
import { useApplicationStore } from '../stores';
import Detail from './detail';
import KeyValueModal from './key-value/KeyValueProIndex';
import CreateNetwork2 from './network2';
import DomainForm from '../../../components/domain-form';

const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const createNetWorkKey = Modal.key();
const createDomainKey = Modal.key();
const modalStyle2 = {
  width: 'calc(100vw - 3.52rem)',
};
const modalStyle3 = {
  width: 740,
};

const AppModals = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    resourceStore,
    treeDs,
  } = useResourceStore();
  const {
    tabs: {
      NET_TAB,
      MAPPING_TAB,
      CIPHER_TAB,
    },
    baseInfoDs,
    mappingStore,
    cipherStore,
    networkStore,
    netDs,
    mappingDs,
    cipherDs,
    appStore,
    checkAppExist,
  } = useApplicationStore();
  const { id, parentId } = resourceStore.getSelectedMenu;

  function refresh() {
    checkAppExist().then((query) => {
      if (query) {
        treeDs.query();
        baseInfoDs.query();
        const current = appStore.getTabKey;
        switch (current) {
          case NET_TAB:
            netDs.query();
            break;
          case MAPPING_TAB:
            mappingDs.query();
            break;
          case CIPHER_TAB:
            cipherDs.query();
            break;
          default:
        }
      }
    });
  }

  function setTabKey(key) {
    const current = appStore.getTabKey;
    if (current !== key) {
      appStore.setTabKey(key);
    }
    refresh();
  }

  function openDetail() {
    const record = baseInfoDs.current;

    if (!record) return;

    Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.service.detail` }),
      children: <Detail
        record={record}
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

  function openKeyValue(type) {
    Modal.open({
      key: modalKey2,
      style: modalStyle2,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.${type}.create` }),
      children: <KeyValueModal
        intlPrefix={intlPrefix}
        modeSwitch={type === MAPPING_TAB}
        title={type}
        envId={parentId}
        appId={id}
        store={type === MAPPING_TAB ? mappingStore : cipherStore}
        refresh={() => setTabKey(type)}
      />,
      okText: formatMessage({ id: 'create' }),
    });
  }

  function saveNetworkIds(ids) {
    const {
      getTabKey,
      setNetworkIds,
    } = appStore;
    if (getTabKey === 'net') {
      setNetworkIds(ids);
    }
  }

  function openNetWork() {
    Modal.open({
      key: createNetWorkKey,
      title: formatMessage({ id: 'network.header.create' }),
      style: { width: 740 },
      okText: formatMessage({ id: 'create' }),
      drawer: true,
      children: <CreateNetwork2
        envId={parentId}
        appId={id}
        networkStore={networkStore}
        refresh={refresh}
      />,
    });
  }

  function openDomain() {
    Modal.open({
      key: createDomainKey,
      style: modalStyle3,
      drawer: true,
      title: formatMessage({ id: 'domain.create.head' }),
      children: <DomainForm
        envId={parentId}
        appServiceId={id}
        refresh={refresh}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        saveNetworkIds={saveNetworkIds}
      />,
      okText: formatMessage({ id: 'create' }),
    });
  }

  function getButtons() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    const notReady = !baseInfoDs.current;
    const disabled = !connect || notReady;

    return [{
      permissions: ['choerodon.code.project.deploy.app-deployment.resource.ps.network'],
      disabled,
      name: formatMessage({ id: `${intlPrefix}.create.network` }),
      icon: 'playlist_add',
      handler: openNetWork,
      display: true,
      group: 1,
    }, {
      permissions: ['choerodon.code.project.deploy.app-deployment.resource.ps.domain'],
      disabled,
      name: formatMessage({ id: `${intlPrefix}.create.ingress` }),
      icon: 'playlist_add',
      handler: openDomain,
      display: true,
      group: 1,
    }, {
      permissions: ['choerodon.code.project.deploy.app-deployment.resource.ps.configmap'],
      disabled,
      name: formatMessage({ id: `${intlPrefix}.create.configMap` }),
      icon: 'playlist_add',
      handler: () => openKeyValue(MAPPING_TAB),
      display: true,
      group: 1,
    }, {
      permissions: ['choerodon.code.project.deploy.app-deployment.resource.ps.cipher'],
      disabled,
      name: formatMessage({ id: `${intlPrefix}.create.cipher` }),
      icon: 'playlist_add',
      handler: () => openKeyValue(CIPHER_TAB),
      display: true,
      group: 1,
    }, {
      disabled: notReady,
      name: formatMessage({ id: `${intlPrefix}.service.detail` }),
      icon: 'find_in_page',
      handler: openDetail,
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

  return (<div>
    <HeaderButtons items={getButtons()} />
  </div>);
});

export default AppModals;
