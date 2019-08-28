import React, { useMemo, useCallback, useEffect, useState } from 'react';
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
const modalKey3 = Modal.key();

const AppModals = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    resourceStore,
  } = useResourceStore();
  const {
    appStore: {
      tabKey,
    },
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
  } = useApplicationStore();
  const { menuId, parentId } = resourceStore.getSelectedMenu;

  const [showKeyValue, setShowKeyValue] = useState(false);
  const [showDomain, setShowDomain] = useState(false);
  const [showNetwork, setShowNetwork] = useState(false);

  function refresh() {
    switch (tabKey) {
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

  function openDetail() {
    Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.service.detail` }),
      children: <Detail
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

  function openKeyValue(type) {
    setShowKeyValue(type);
  }

  function closeKeyValue(isLoad) {
    setShowKeyValue(false);
    isLoad && refresh();
  }

  function closeDomain(isLoad) {
    setShowDomain(false);
    isLoad && refresh();
  }

  function closeNetwork(isLoad) {
    setShowNetwork(false);
    isLoad && refresh();
  }

  function getButtons() {
    return [{
      name: formatMessage({ id: `${intlPrefix}.create.network` }),
      icon: 'playlist_add',
      handler: () => setShowNetwork(true),
      display: tabKey === NET_TAB,
      group: 1,
    }, {
      name: formatMessage({ id: `${intlPrefix}.create.ingress` }),
      icon: 'playlist_add',
      handler: () => setShowDomain(true),
      display: tabKey === NET_TAB,
      group: 1,
    }, {
      name: formatMessage({ id: `${intlPrefix}.create.configMap` }),
      icon: 'playlist_add',
      handler: () => openKeyValue('configMap'),
      display: tabKey === MAPPING_TAB,
      group: 1,
    }, {
      name: formatMessage({ id: `${intlPrefix}.create.secret` }),
      icon: 'playlist_add',
      handler: () => openKeyValue('secret'),
      display: tabKey === CIPHER_TAB,
      group: 1,
    }, {
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
    {showKeyValue && <KeyValueModal
      modeSwitch={showKeyValue === 'configMap'}
      title={showKeyValue}
      visible={!!showKeyValue}
      envId={parentId}
      appId={menuId}
      onClose={closeKeyValue}
      store={showKeyValue === 'configMap' ? mappingStore : cipherStore}
    />}
    {showDomain && (
      <DomainModal
        envId={parentId}
        appServiceId={menuId}
        visible={showDomain}
        type="create"
        store={domainStore}
        onClose={closeDomain}
      />
    )}
    {showNetwork && (
      <CreateNetwork
        envId={parentId}
        appServiceId={menuId}
        visible={showNetwork}
        store={networkStore}
        onClose={closeNetwork}
      />
    )}
  </div>);
});

export default AppModals;
