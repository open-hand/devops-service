import React, { useMemo, useCallback, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import HeaderButtons from '../../../components/header-buttons';
import { useResourceStore } from '../../../../stores';
import { useApplicationStore } from '../stores';
import { useModalStore } from './stores';
import Detail from './detail';
import KeyValueModal from './key-value';
import DomainModal from './domain';
import CreateNetwork from './network';

const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalKey3 = Modal.key();
const modalStyle = {
  width: '26%',
};

const AppModals = observer(() => {
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
  } = useApplicationStore();
  const {
    modalStore,
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();
  const { menuId, parentId } = resourceStore.getSelectedMenu;

  const [showKeyValue, setShowKeyValue] = useState(false);
  const [showDomain, setShowDomain] = useState(false);
  const [showNetwork, setShowNetwork] = useState(false);

  const openModal = useCallback(() => {
    // console.log(modal);
  }, []);

  useEffect(() => {
    resourceStore.setNoHeader(false);
  }, [resourceStore]);

  function refresh() {
  }

  function openDetail() {
    const detailModal = Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.service.detail` }),
      children: <Detail record={baseInfoDs.current} intlPrefix={intlPrefix} prefixCls={prefixCls} formatMessage={formatMessage} />,
      drawer: true,
      style: modalStyle,
      footer: (
        <Button funcType="raised" type="primary" onClick={() => detailModal.close()}>
          <FormattedMessage id="close" />
        </Button>
      ),
    });
  }

  function openKeyValue(type) {
    setShowKeyValue(type);
  }

  function closeKeyValue(isLoad) {
    setShowKeyValue(false);
  }

  function closeDomain(isLoad) {
    setShowDomain(false);
  }

  function closeNetwok(isLoad) {
    setShowNetwork(false);
  }

  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.create.network` }),
    icon: 'playlist_add',
    handler: () => setShowNetwork(true),
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: `${intlPrefix}.create.ingress` }),
    icon: 'playlist_add',
    handler: () => setShowDomain(true),
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: `${intlPrefix}.create.configMap` }),
    icon: 'playlist_add',
    handler: () => openKeyValue('configMap'),
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: `${intlPrefix}.create.secret` }),
    icon: 'playlist_add',
    handler: () => openKeyValue('secret'),
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: `${intlPrefix}.service.detail` }),
    icon: 'find_in_page',
    handler: openDetail,
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: 'refresh' }),
    icon: 'refresh',
    handler: refresh,
    display: true,
    group: 1,
  }]), [formatMessage, intlPrefix, openDetail]);

  return (<div>
    <HeaderButtons items={buttons} />
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
        onClose={closeNetwok}
      />
    )}
  </div>);
});

export default AppModals;
