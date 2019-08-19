import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import BaseInfoDataSet from './BaseInfoDataSet';
import NetDataSet from './NetDataSet';
import ConfigDataSet from './ConfigDataSet';
import { useResourceStore } from '../../../../stores';
import useStore from './useStore';
import useConfigMapStore from './useConfigMapStore';
import useSecretStore from './useSecretStore';
import useDomainStore from './useDomainStore';
import useNetworkStore from './useNetworkStore';

const Store = createContext();

export function useApplicationStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(observer((props) => {
  const { children, intl: { formatMessage } } = props;
  const {
    AppState: { currentMenuType: { id } },
    resourceStore: { getSelectedMenu: { menuId, parentId } },
    intlPrefix,
    resourceStore,
  } = useResourceStore();
  const tabs = useMemo(() => ({
    NET_TAB: 'net',
    MAPPING_TAB: 'mapping',
    CIPHER_TAB: 'cipher',
  }), []);
  const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);
  const netDs = useMemo(() => new DataSet(NetDataSet({
    formatMessage,
    intlPrefix,
    projectId: id,
    id: menuId,
  })), [id, menuId]);
  const mappingDs = useMemo(() => new DataSet(ConfigDataSet({
    formatMessage,
    intlPrefix,
    projectId: id,
    envId: parentId,
    appId: menuId,
    type: tabs.MAPPING_TAB,
  })), [id, menuId, parentId]);
  const cipherDs = useMemo(() => new DataSet(ConfigDataSet({
    formatMessage,
    intlPrefix,
    projectId: id,
    envId: parentId,
    appId: menuId,
    type: tabs.CIPHER_TAB,
  })), [id, menuId, parentId]);

  const appStore = useStore(tabs);
  const mappingStore = useConfigMapStore();
  const cipherStore = useSecretStore();
  const domainStore = useDomainStore();
  const networkStore = useNetworkStore();


  useEffect(() => {
    baseInfoDs.transport.read.url = `/devops/v1/projects/${id}/app_service/${menuId}`;
    baseInfoDs.query();
  }, [id, menuId]);

  const tabKey = appStore.getTabKey;
  useEffect(() => {
    switch (tabKey) {
      case tabs.NET_TAB:
        netDs.query();
        break;
      case tabs.MAPPING_TAB:
        mappingDs.query();
        break;
      case tabs.CIPHER_TAB:
        cipherDs.query();
        break;
      default:
    }
  }, [id, menuId, tabKey]);

  const value = {
    ...props,
    tabs,
    baseInfoDs,
    netDs,
    mappingDs,
    cipherDs,
    appStore,
    mappingStore,
    cipherStore,
    domainStore,
    networkStore,
  };
  return (
    <Store.Provider value={value}>
      {children}
    </Store.Provider>
  );
}));
