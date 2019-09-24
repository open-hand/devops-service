import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import setTreeMenuSelect from '../../../../utils/setTreeMenuSelect';
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
    setTreeMenuSelect(treeDs, clusterStore);
  }, [treeDs.data]);

  return <nav style={bounds} className="c7ncd-cluster-sidebar">
    <TreeView
      ds={treeDs}
      store={clusterStore}
      nodesRender={nodeRenderer}
    />
  </nav>;
});

export default TreeMenu;
