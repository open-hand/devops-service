import React, { Fragment, useRef, lazy, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import DragBar from '../../../components/drag-bar';
import Loading from '../../../components/loading';
import Sidebar from './sidebar';
import { useEnvironmentStore } from '../stores';
import { useMainStore } from './stores';

import './index.less';

const Group = lazy(() => import('./contents/group'));
const Detail = lazy(() => import('./contents/detail'));
const EmptyPage = lazy(() => import('./contents/empty'));

const MainView = observer(() => {
  const {
    prefixCls,
    envStore: { getSelectedMenu: { itemType } },
    itemType: {
      DETAIL_ITEM,
      GROUP_ITEM,
    },
    treeDs,
  } = useEnvironmentStore();
  const { mainStore } = useMainStore();
  const rootRef = useRef();

  const content = useMemo(() => {
    const cmMaps = {
      [GROUP_ITEM]: <Group />,
      [DETAIL_ITEM]: <Detail />,
    };
    return cmMaps[itemType]
      ? <Suspense fallback={<Loading display />}>{cmMaps[itemType]}</Suspense>
      : <Loading display />;
  }, [itemType]);

  function getMainView() {
    if (!treeDs.length) {
      return <div
        className={`${prefixCls}-wrap`}
      >
        <Suspense fallback={<Loading display />}>
          <EmptyPage />
        </Suspense>
        <div>请先创建分组！</div>
      </div>;
    } else {
      return <div
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
      </div>;
    }
  }

  return treeDs.status === 'ready' ? getMainView() : <Loading display />;
});

export default MainView;
