import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import SidebarHeading from './header';
import TreeView from '../../../../components/tree-view';
import TreeItem from './tree-item';
import { useResourceStore } from '../../stores';
import { useMainStore } from '../stores';

import './index.less';

const TreeMenu = observer(() => {
  const {
    treeDs,
    prefixCls,
    resourceStore,
  } = useResourceStore();
  const { mainStore } = useMainStore();

  const bounds = useMemo(() => mainStore.getNavBounds, [mainStore.getNavBounds]);
  const nodeRenderer = useCallback((record, search) => <TreeItem record={record} search={search} />, []);

  useEffect(() => {
    if (treeDs.length) {
      const selectedRecord = treeDs.find((record) => record.isSelected);
      const { key: selectedKey, parentId: selectedParentId } = resourceStore.getSelectedMenu;

      /**
       *
       * 设置默认选中项和选中项丢失后的处理
       *
       *   记录中没有选中项或者选中项和store中保存的项不匹配 --则需要进行重新选择
       *
       *      先从记录中找到匹配的项，找到则直接设置为选中
       *      否则查找该项的父节点设置为选中
       *
       *   如果无父节点则选中所有记录中的第一项
       *
       *   否则清空选中
       *
       * */

      if (!selectedRecord || selectedRecord.get('key') !== selectedKey) {
        const prevSelected = treeDs.find((record) => record.get('key') === selectedKey);
        if (prevSelected) {
          prevSelected.isSelected = true;
        } else {
          const parent = treeDs.find((record) => record.get('key') === selectedParentId);
          if (parent) {
            parent.isSelected = true;
          } else {
            const first = treeDs.get(0);
            if (first) {
              first.isSelected = true;
              const selectedId = first.get('id');
              const itemType = first.get('itemType');
              const parentId = first.get('parentId');
              const key = first.get('key');
              resourceStore.setSelectedMenu({
                menuId: selectedId,
                menuType: itemType,
                parentId,
                key,
              });
            } else {
              resourceStore.setSelectedMenu({});
            }
          }
        }
      }
    }
  }, [treeDs.length]);

  return <nav style={bounds} className={`${prefixCls}-sidebar`}>
    <SidebarHeading />
    <div className={`${prefixCls}-sidebar-menu`}>
      <TreeView
        ds={treeDs}
        store={resourceStore}
        nodesRender={nodeRenderer}
      />
    </div>
  </nav>;
});

export default TreeMenu;
