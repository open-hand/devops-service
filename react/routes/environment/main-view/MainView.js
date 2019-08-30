import React, { Fragment, useRef, lazy, Suspense } from 'react';
import { observer } from 'mobx-react-lite';
import isEmpty from 'lodash/isEmpty';
import DragBar from '../../../components/drag-bar';
import Loading from '../../../components/loading';
import Sidebar from './sidebar';
import { useEnvironmentStore } from '../stores';
import { useMainStore } from './stores';

import './index.less';

const Group = lazy(() => import('./contents/group'));
const Detail = lazy(() => import('./contents/detail'));

const MainView = observer(() => {
  const {
    prefixCls,
    envStore: { getSelectedMenu },
    itemType: {
      DETAIL_ITEM,
      GROUP_ITEM,
    },
  } = useEnvironmentStore();
  const { mainStore } = useMainStore();
  const rootRef = useRef();

  function getContent() {
    const { itemType } = getSelectedMenu;
    const cmMaps = {
      [GROUP_ITEM]: <Group />,
      [DETAIL_ITEM]: <Detail />,
    };
    return cmMaps[itemType]
      ? <Suspense fallback={<Loading display />}>{cmMaps[itemType]}</Suspense>
      : <Loading display />;
  }

  return (<div
    ref={rootRef}
    className={`${prefixCls}-wrap`}
  >
    <DragBar
      parentRef={rootRef}
      store={mainStore}
    />
    <Fragment>
      <Sidebar />
      {!isEmpty(getSelectedMenu) ? <div className={`${prefixCls}-main ${prefixCls}-animate`}>
        {getContent()}
      </div> : <Loading display />}
    </Fragment>
  </div>);
});

export default MainView;
