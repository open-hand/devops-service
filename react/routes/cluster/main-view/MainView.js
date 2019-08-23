import React, { Fragment, useRef, useMemo, lazy, Suspense } from 'react';
import { observer } from 'mobx-react-lite';
import isEmpty from 'lodash/isEmpty';
import classnames from 'classnames';
import Draggable from 'react-draggable';
import Sidebar from './sidebar';
import LoadingBar from '../../../components/loading';
import { useClusterStore } from '../stores';
import { useClusterMainStore } from './stores';
import { useResize, X_AXIS_WIDTH, X_AXIS_WIDTH_MAX } from './useResize';
import './styles/index.less';

const ClusterContent = lazy(() => import('./contents/cluster-content'));
const NodeContent = lazy(() => import('./contents/node-content'));

export default observer((props) => {
  const {
    prefixCls,
    clusterStore: {
      getViewType,
      getSelectedMenu,
    },
    itemType: {
      CLU_ITEM,
      NODE_ITEM,
    },
  } = useClusterStore();
  const { mainStore } = useClusterMainStore();
  const rootRef = useRef(null);

  const { menuType } = getSelectedMenu;
  const content = useMemo(() => {
    const cmMaps = {
      [CLU_ITEM]: <ClusterContent />,
      [NODE_ITEM]: <NodeContent />,
    };
    return cmMaps[menuType]
      ? <Suspense fallback={<div>loading</div>}>{cmMaps[menuType]}</Suspense>
      : <div>加载中</div>;
  }, [menuType]);

  const {
    isDragging,
    bounds,
    resizeNav,
    draggable,
    handleUnsetDrag,
    handleStartDrag,
    handleDrag,
  } = useResize(rootRef, mainStore);

  const dragPrefixCls = `${prefixCls}-draggers`;
  const draggableClass = useMemo(() => classnames({
    [`${dragPrefixCls}-handle`]: true,
    [`${dragPrefixCls}-handle-dragged`]: isDragging,
  }), [isDragging]);

  const dragRight = resizeNav.x >= X_AXIS_WIDTH_MAX ? X_AXIS_WIDTH_MAX : bounds.width - X_AXIS_WIDTH;

  return (<div
    ref={rootRef}
    className={`${prefixCls}-wrap`}
  >
    {draggable && (
      <Fragment>
        <Draggable
          axis="x"
          position={resizeNav}
          bounds={{
            left: X_AXIS_WIDTH,
            right: dragRight,
            top: 0,
            bottom: 0,
          }}
          onStart={handleStartDrag}
          onDrag={handleDrag}
          onStop={handleUnsetDrag}
        >
          <div className={draggableClass} />
        </Draggable>
        {isDragging && <div className={`${dragPrefixCls}-blocker`} />}
      </Fragment>
    )}
    <Fragment>
      <Sidebar />
      {!isEmpty(getSelectedMenu) ? <div className={`${prefixCls}-main ${dragPrefixCls}-animate`}>
        {content}
      </div> : <LoadingBar display />}
    </Fragment>
  </div>);
});
