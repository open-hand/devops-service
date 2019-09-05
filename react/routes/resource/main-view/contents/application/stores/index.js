import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import getTablePostData from '../../../../../../utils/getTablePostData';
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
    AppState: { currentMenuType: { id: projectId } },
    resourceStore: { getSelectedMenu: { id, parentId } },
    intlPrefix,
  } = useResourceStore();
  const tabs = useMemo(() => ({
    NET_TAB: 'net',
    MAPPING_TAB: 'mapping',
    CIPHER_TAB: 'cipher',
  }), []);
  const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);
  const netDs = useMemo(() => new DataSet(NetDataSet({ formatMessage, intlPrefix })), [projectId, id]);
  const mappingDs = useMemo(() => new DataSet(ConfigDataSet({
    formatMessage,
    intlPrefix,
    projectId,
    envId: parentId,
    appId: id,
    type: tabs.MAPPING_TAB,
  })), [projectId, id, parentId]);
  const cipherDs = useMemo(() => new DataSet(ConfigDataSet({
    formatMessage,
    intlPrefix,
    projectId,
    envId: parentId,
    appId: id,
    type: tabs.CIPHER_TAB,
  })), [projectId, id, parentId]);

  const appStore = useStore(tabs);
  const mappingStore = useConfigMapStore();
  const cipherStore = useSecretStore();
  const domainStore = useDomainStore();
  const networkStore = useNetworkStore();


  useEffect(() => {
    baseInfoDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service/${id}`;
    baseInfoDs.query();
    netDs.transport.read = ({ data }) => {
      const postData = getTablePostData(data);
      return ({
        url: `/devops/v1/projects/${projectId}/service/page_by_instance?app_service_id=${id}`,
        method: 'post',
        data: postData,
      });
    };
    netDs.transport.destroy = ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/service/${data.id}`,
      method: 'delete',
    });
    netDs.query();
  }, [projectId, id]);

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
  }, [tabKey]);

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
