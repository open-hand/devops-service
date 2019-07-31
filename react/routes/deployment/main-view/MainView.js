import React, { Fragment, useContext, useState, useRef, useEffect, useMemo, lazy, Suspense } from 'react';
import { observer } from 'mobx-react-lite';
import throttle from 'lodash/throttle';
import classnames from 'classnames';
import Draggable from 'react-draggable';
import Sidebar from './sidebar';
import Store from './stores';
import DeploymentStore from '../stores';

import './styles/index.less';
import './styles/draggers.less';

const MARGIN = 10;
const X_AXIS_WIDTH = 220;
const X_AXIS_WIDTH_MAX = 320;
const MAIN_WIDTH_MIN = 200;

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

  const [bounds, setBounds] = useState({});
  const [isDragging, setIsDragging] = useState(false);
  const [draggable, setDraggable] = useState(true);
  const [resizeNav, setResizeNav] = useState({ x: X_AXIS_WIDTH, y: 0 });

  useEffect(() => {
    const getRootBounds = throttle(() => {
      const { current } = rootRef;

      if (current) {
        const { offsetWidth, offsetHeight } = current;

        setBounds({
          width: offsetWidth,
          height: offsetHeight,
        });
      }
    }, 100);

    getRootBounds();
    window.addEventListener('resize', getRootBounds, true);
    return () => {
      window.removeEventListener('resize', getRootBounds);
    };
  }, []);

  const handleDrag = (e, data) => {
    if (data.deltaX) {
      setResizeNav({ x: data.x, y: data.y });
    }
  };

  const handleStartDrag = () => {
    setIsDragging(true);
  };

  const handleUnsetDrag = () => {
    setIsDragging(false);
  };

  useEffect(() => {
    const navX = resizeNav.x;
    const computedLeft = bounds.width - MAIN_WIDTH_MIN;
    let nextResize = resizeNav;

    if (computedLeft < X_AXIS_WIDTH || navX < X_AXIS_WIDTH) {
      nextResize = {
        ...resizeNav,
        x: X_AXIS_WIDTH,
      };
    } else if (navX > X_AXIS_WIDTH_MAX) {
      nextResize = {
        ...resizeNav,
        x: X_AXIS_WIDTH_MAX,
      };
    } else if (computedLeft < navX) {
      nextResize = {
        ...resizeNav,
        x: computedLeft,
      };
    }

    setResizeNav(nextResize);

    const navBounds = {
      width: nextResize.x + MARGIN,
      height: bounds.height,
    };

    setDraggable(computedLeft > X_AXIS_WIDTH);
    store.setNavBounds(navBounds);
  }, [bounds.height, bounds.width, resizeNav, resizeNav.x, store]);

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
