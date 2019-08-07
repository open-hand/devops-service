import React, { createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import InstanceTreeDataSet from './InstanceTreeDataSet';
import ResourceTreeDataSet from './ResourceTreeDataSet';
import { useDeploymentStore } from '../../../stores';
import useStore from './useStore';

const Store = createContext();

export function useSidebarStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer(
    (props) => {
      const { AppState: { currentMenuType: { id } }, children } = props;
      const {
        deploymentStore,
        viewType: {
          IST_VIEW_TYPE,
          RES_VIEW_TYPE,
        },
      } = useDeploymentStore();
      const sidebarStore = useStore();
      const viewType = deploymentStore.getViewType;

      const treeDs = useMemo(() => {
        const treeMapping = {
          [IST_VIEW_TYPE]: InstanceTreeDataSet,
          [RES_VIEW_TYPE]: ResourceTreeDataSet,
        };
        const TreeDataSet = treeMapping[viewType];
        return new DataSet(TreeDataSet(id, deploymentStore, sidebarStore));
      }, [IST_VIEW_TYPE, RES_VIEW_TYPE, deploymentStore, id, sidebarStore, viewType]);

      const value = {
        ...props,
        treeDs,
        sidebarStore,
      };
      return (
        <Store.Provider value={value}>
          {children}
        </Store.Provider>
      );
    }
  )
));
