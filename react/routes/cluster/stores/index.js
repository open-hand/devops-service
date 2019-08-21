import React, { createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from './useStore';
import { viewTypeMappings, itemTypeMappings } from './mappings';

const Store = createContext();

export function useClusterStore() {
  return useContext(Store);
}


export const StoreProvider = injectIntl(inject('AppState')(observer(
  (props) => {
    const clusterStore = useStore();
    const resourceStore = useStore();
    const viewType = resourceStore.getViewType;
    const viewTypeMemo = useMemo(() => viewTypeMappings, []);
    const itemType = useMemo(() => itemTypeMappings, []);
    // const treeDs = useMemo(() => new DataSet(TreeDataSet(resourceStore, viewType)), [viewType]);
    const { children } = props;
    const value = {
      ...props,
      prefixCls: 'c7ncd-cluster',
      intlPrefix: 'c7ncd.cluster',
      permissions: [],
      clusterStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  } 
)));
