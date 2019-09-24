import React, { useRef, useMemo, lazy, Suspense } from 'react';
import { observer } from 'mobx-react-lite';
import { FormattedMessage } from 'react-intl';
import Sidebar from './sidebar';
import DragBar from '../../../components/drag-bar';
import Loading from '../../../components/loading';
import { useClusterStore } from '../stores';
import { useClusterMainStore } from './stores';

import './index.less';

const ClusterContent = lazy(() => import('./contents/cluster-content'));
const NodeContent = lazy(() => import('./contents/node-content'));
const EmptyPage = lazy(() => import('./contents/empty'));

export default observer(() => {
  const {
    prefixCls,
    clusterStore: {
      getSelectedMenu: { itemType },
    },
    itemType: {
      CLU_ITEM,
      NODE_ITEM,
    },
    treeDs,
  } = useClusterStore();
  const { mainStore } = useClusterMainStore();
  const rootRef = useRef(null);

  const content = useMemo(() => {
    const cmMaps = {
      [CLU_ITEM]: <ClusterContent />,
      [NODE_ITEM]: <NodeContent />,
    };
    return cmMaps[itemType]
      ? <Suspense fallback={<Loading display />}>{cmMaps[itemType]}</Suspense>
      : <Loading display />;
  }, [itemType]);

  if (!treeDs.length && treeDs.status === 'ready') {
    return <div
      className={`${prefixCls}-wrap`}
    >
      <Suspense fallback={<Loading display />}>
        <EmptyPage />
      </Suspense>
      <div><FormattedMessage id="c7ncd.cluster.empty.msg" /></div>
    </div>;
  } else {
    return (<div
      ref={rootRef}
      className={`${prefixCls}-wrap`}
    >
      <DragBar
        parentRef={rootRef}
        store={mainStore}
      />
      <Sidebar />
      <div className={`${prefixCls}-main ${prefixCls}-animate`}>
        {content}
      </div>
    </div>);
  }
});
