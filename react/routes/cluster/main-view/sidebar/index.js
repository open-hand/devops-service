import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import TreeView from '../../../../components/tree-view';
import TreeItem from './tree-item';
import { useClusterStore } from '../../stores';
import { useClusterMainStore } from '../stores';

import './index.less';

const TreeMenu = observer(() => {
  const {
    treeDs,
    clusterStore,
  } = useClusterStore();
  const { mainStore } = useClusterMainStore();

  const bounds = useMemo(() => mainStore.getNavBounds, [mainStore.getNavBounds]);
  const nodeRenderer = useCallback((record, search) => <TreeItem record={record} search={search} />, []);

  useEffect(() => {
    const recordList = treeDs.data;

    if (recordList.length) {
      const selectedRecord = treeDs.find((record) => record.isSelected);
      const { key: selectedKey } = clusterStore.getSelectedMenu;

      // 记录中没有选中项或者选中项和store中保存的项不匹配
      if (!selectedRecord || selectedRecord.get('key') !== selectedKey) {
        const prevSelected = treeDs.find((record) => record.get('key') === selectedKey);
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
            clusterStore.setSelectedMenu({
              menuId: selectedId,
              menuType: itemType,
              parentId,
              key,
            });
          } else {
            clusterStore.setSelectedMenu({});
          }
        }
      }
    }
  }, [treeDs.data]);

  return <nav style={bounds} className="c7ncd-deployment-sidebar">
    <TreeView
      ds={treeDs}
      store={clusterStore}
      nodesRender={nodeRenderer}
    />
  </nav>;
});

export default TreeMenu;
