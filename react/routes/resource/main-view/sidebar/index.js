import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import SidebarHeading from './header';
import setTreeMenuSelect from '../../../../utils/setTreeMenuSelect';
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
    setTreeMenuSelect(treeDs, resourceStore);
  }, [treeDs.data]);

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
