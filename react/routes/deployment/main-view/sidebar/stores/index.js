import React, { createContext, useMemo, useContext, useEffect } from 'react';
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
      }, [id, viewType]);

      useEffect(() => {
        const recordList = treeDs.data;

        if (recordList.length) {
          const selectedRecord = treeDs.find(record => record.isSelected);
          const { key: selectedKey } = deploymentStore.getSelectedMenu;

          // 记录中没有选中项或者选中项和store中保存的项不匹配
          if (!selectedRecord || selectedRecord.get('key') !== selectedKey) {
            const prevSelected = treeDs.find(record => record.get('key') === selectedKey);
            // 记录中寻找匹配的项
            // 找到则设置为选中，否则设置第一个记录为选中
            if (prevSelected) {
              prevSelected.isSelected = true;
            } else {
              const first = treeDs.get(0);
              if (first) {
                first.isSelected = true;
                const selectedId = first.get('id');
                const itemType = first.get('itemType');
                const parentId = first.get('parentId');
                const key = first.get('key');
                deploymentStore.setSelectedMenu({
                  menuId: selectedId,
                  menuType: itemType,
                  parentId,
                  key,
                });
              } else {
                deploymentStore.setSelectedMenu({});
              }
            }
          }
        }
      }, [treeDs.data]);

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
