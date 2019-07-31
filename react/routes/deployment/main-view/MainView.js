import React, { Fragment, useContext, useState, useRef, useEffect, useMemo, lazy, Suspense } from 'react';
import { observer } from 'mobx-react-lite';
import classnames from 'classnames';
import Draggable from 'react-draggable';
import Sidebar from './sidebar';
import Store from './stores';
import DeploymentStore from '../stores';
import { useResize, X_AXIS_WIDTH, X_AXIS_WIDTH_MAX } from './useResize';

import './styles/index.less';
import './styles/draggers.less';

// 实例视图
const EnvContent = lazy(() => import('./contents/instance-view/environment'));
const AppContent = lazy(() => import('./contents/instance-view/application'));
const IstContent = lazy(() => import('./contents/instance-view/instance'));

const MainView = observer(() => {
  const { prefixCls } = useContext(DeploymentStore);
  const {
    instanceView: {
      ENV_ITEM,
      APP_ITEM,
      IST_ITEM,
    },
    store,
  } = useContext(Store);
  const content = useMemo(() => {
    const { menuType } = store.getSelectedMenu;
    const cmMaps = {
      [ENV_ITEM]: <EnvContent />,
      [APP_ITEM]: <AppContent />,
      [IST_ITEM]: <IstContent />,
    };

    return cmMaps[menuType]
      ? <Suspense fallback={<div>loading</div>}>{cmMaps[menuType]}</Suspense>
      : <div>加载数据中</div>;
  }, [APP_ITEM, ENV_ITEM, IST_ITEM, store.getSelectedMenu]);

  const rootRef = useRef(null);

  const {
    isDragging,
    bounds,
    resizeNav,
    draggable,
    handleUnsetDrag,
    handleStartDrag,
    handleDrag,
  } = useResize(rootRef, store);

  const draggableClass = useMemo(() => classnames({
    'c7n-draggers-handle': true,
    'c7n-draggers-handle-dragged': isDragging,
  }), [isDragging]);

  return (<div
    ref={rootRef}
    className={`${prefixCls}-wrap`}
  >
    {!draggable ? null : (
      <Fragment>
        <Draggable
          axis="x"
          position={resizeNav}
          bounds={{
            left: X_AXIS_WIDTH,
            top: 0,
            right: resizeNav.x >= X_AXIS_WIDTH_MAX ? X_AXIS_WIDTH_MAX : bounds.width - X_AXIS_WIDTH,
            bottom: 0,
          }}
          onStart={handleStartDrag}
          onDrag={handleDrag}
          onStop={handleUnsetDrag}
        >
          <div className={draggableClass} />
        </Draggable>
        {isDragging ? <div className="c7n-draggers-blocker" /> : null}
      </Fragment>
    )}
    <Fragment>
      <Sidebar />
      <div className={`${prefixCls}-main c7n-draggers-animate`}>
        {content}
      </div>
    </Fragment>
  </div>);
});

export default MainView;
