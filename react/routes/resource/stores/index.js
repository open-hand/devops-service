import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { viewTypeMappings, itemTypeMappings } from './mappings';
import TreeDataSet from './TreeDataSet';
import useStore from './useStore';

const Store = createContext();

export function useDeploymentStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    const deploymentStore = useStore();
    const viewTypeMemo = useMemo(() => viewTypeMappings, []);
    const itemType = useMemo(() => itemTypeMappings, []);
    const viewType = deploymentStore.getViewType;
    const treeDs = useMemo(() => new DataSet(TreeDataSet(id, deploymentStore, viewType)), [id, viewType]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-deployment',
      intlPrefix: 'c7ncd.deployment',
      permissions: [],
      viewTypeMappings: viewTypeMemo,
      itemType,
      deploymentStore,
      treeDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }),
));
