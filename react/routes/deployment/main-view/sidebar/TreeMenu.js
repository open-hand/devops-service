import React, { useMemo, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import SidebarHeading from './header';
import TreeView from '../../../../components/tree-view';
import TreeItem from './tree-item';
import { useSidebarStore } from './stores';
import { useMainStore } from '../stores';

import './index.less';

const TreeMenu = observer(() => {
  const {
    treeDs,
    sidebarStore,
  } = useSidebarStore();
  const { mainStore } = useMainStore();

  const bounds = useMemo(() => mainStore.getNavBounds, [mainStore.getNavBounds]);
  const nodeRenderer = useCallback((record, search) => <TreeItem record={record} search={search} />, []);

  return <nav style={bounds} className="c7ncd-deployment-sidebar">
    <SidebarHeading />
    <TreeView
      ds={treeDs}
      store={sidebarStore}
      nodesRender={nodeRenderer}
    />
  </nav>;
});

export default TreeMenu;
