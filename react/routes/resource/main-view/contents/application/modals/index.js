import React, { useMemo, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useResourceStore } from '../../../../stores';
import { useApplicationStore } from '../stores';
import Detail from './detail';
import KeyValueModal from './key-value';
import DomainModal from './domain';
import CreateNetwork from './network';

const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalStyle2 = {
  width: 'calc(100vw - 3.52rem)',
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
    domainStore,
    networkStore,
    netDs,
    mappingDs,
    cipherDs,
    appStore,
    checkAppExist,
  } = useApplicationStore();
  const { id, parentId } = resourceStore.getSelectedMenu;

  const [showDomain, setShowDomain] = useState(false);
  const [showNetwork, setShowNetwork] = useState(false);

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

  function closeDomain(isLoad) {
    setShowDomain(false);
    isLoad && setTabKey(NET_TAB);
  }

  function closeNetwork(isLoad) {
    setShowNetwork(false);
    isLoad && setTabKey(NET_TAB);
  }

  function getButtons() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    const notReady = !baseInfoDs.current;
    const disabled = !connect || notReady;

    return [{
      disabled,
      name: formatMessage({ id: `${intlPrefix}.create.network` }),
      icon: 'playlist_add',
      handler: () => setShowNetwork(true),
      display: true,
      group: 1,
    }, {
      disabled,
      name: formatMessage({ id: `${intlPrefix}.create.ingress` }),
      icon: 'playlist_add',
      handler: () => setShowDomain(true),
      display: true,
      group: 1,
    }, {
      disabled,
      name: formatMessage({ id: `${intlPrefix}.create.configMap` }),
      icon: 'playlist_add',
      handler: () => openKeyValue(MAPPING_TAB),
      display: true,
      group: 1,
    }, {
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
    {showDomain && (
      <DomainModal
        envId={parentId}
        appServiceId={id}
        visible={showDomain}
        type="create"
        store={domainStore}
        onClose={closeDomain}
      />
    )}
    {showNetwork && (
      <CreateNetwork
        envId={parentId}
        appServiceId={id}
        visible={showNetwork}
        store={networkStore}
        onClose={closeNetwork}
      />
    )}
  </div>);
});

export default AppModals;
