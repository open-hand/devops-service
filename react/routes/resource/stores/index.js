import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { viewTypeMappings, itemTypeMappings } from './mappings';
import TreeDataSet from './TreeDataSet';
import useStore from './useStore';

const { IST_VIEW_TYPE, RES_VIEW_TYPE } = viewTypeMappings;

const Store = createContext();

export function useResourceStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    const resourceStore = useStore();
    const viewType = resourceStore.getViewType;
    const viewTypeMemo = useMemo(() => viewTypeMappings, []);
    const itemType = useMemo(() => itemTypeMappings, []);
    const treeDs = useMemo(() => new DataSet(TreeDataSet(resourceStore, viewType)), [viewType]);

    useEffect(() => {
      const urlMaps = {
        [IST_VIEW_TYPE]: `/devops/v1/projects/${id}/envs/ins_tree_menu`,
        [RES_VIEW_TYPE]: `/devops/v1/projects/${id}/envs/resource_tree_menu`,
      };
      treeDs.transport.read.url = urlMaps[viewType];
      treeDs.query();
    }, [id, viewType]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-deployment',
      intlPrefix: 'c7ncd.deployment',
      permissions: [],
      viewTypeMappings: viewTypeMemo,
      itemType,
      resourceStore,
      treeDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }),
));
