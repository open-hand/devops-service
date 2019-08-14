import React, { createContext, useContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import BaseInfoDataSet from './BaseInfoDataSet';
import { useDeploymentStore } from '../../../../stores';
import useStore from './useStore';
import useConfigMapStore from './useConfigMapStore';
import useSecretStore from './useSecretStore';
import useDomainStore from './useDomainStore';
import useNetworkStore from './useNetworkStore';

const Store = createContext();

export function useApplicationStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      deploymentStore: { getSelectedMenu: { menuId } },
    } = useDeploymentStore();
    const { AppState: { currentMenuType: { id } }, children } = props;
    
    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet(id, menuId)), [id, menuId]);
    const appStore = useStore();
    const mappingStore = useConfigMapStore();
    const cipherStore = useSecretStore();
    const domainStore = useDomainStore();
    const networkStore = useNetworkStore();

    const value = {
      ...props,
      tabs: {
        NET_TAB: 'net',
        MAPPING_TAB: 'mapping',
        CIPHER_TAB: 'cipher',
      },
      baseInfoDs,
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
  }
));
