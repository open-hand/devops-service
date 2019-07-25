import React, { useContext, useState, useRef, useEffect, useMemo, lazy, Suspense } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import _ from 'lodash';
import LayoutPage from '../components/layout';
import { IST_ITEM, APP_ITEM, ENV_ITEM } from '../components/TreeItemIcon';
import Sidebar from '../sidebar';
import Stores from '../stores';

import './index.less';

const LEFT_OFFSET = 200;
const getNav = ({ navBounds }) => <Sidebar navBounds={navBounds} />;

// 实例视图
const EnvContent = lazy(() => import('../contents/instance-view/environment'));
const AppContent = lazy(() => import('../contents/instance-view/application'));
const IstContent = lazy(() => import('../contents/instance-view/instance'));

const getContent = (type) => {
  let content;
  switch (type) {
    case ENV_ITEM:
      content = () => <Suspense fallback={<div>Error</div>}>
        <EnvContent />
      </Suspense>;
      break;
    case APP_ITEM:
      content = () => <Suspense fallback={<div>Error</div>}>
        <AppContent />
      </Suspense>;
      break;
    case IST_ITEM:
      content = () => <Suspense fallback={<div>Error</div>}>
        <IstContent />
      </Suspense>;
      break;
    default:
      content = () => null;
  }

  return content;
};

const MainView = observer(({ MenuStore }) => {
  const { store } = useContext(Stores);

  const rootRef = useRef(null);
  const [bounds, setBounds] = useState(null);
  const [defaultCollapsed] = useState(() => MenuStore.collapsed);

  const getRootBounds = _.throttle(() => {
    const { current } = rootRef;

    if (current) {
      const { offsetWidth, offsetHeight } = current;

      setBounds({
        width: offsetWidth,
        height: offsetHeight,
      });
    }
  }, 100);

  useEffect(() => {
    getRootBounds();
    window.addEventListener('resize', getRootBounds, true);
    return () => {
      window.removeEventListener('resize', getRootBounds);
    };
  }, []);

  const realBounds = useMemo(() => {
    const computedBounds = { ...bounds };
    if (bounds) {
      // 菜单栏展开关闭
      const menuCollapsed = MenuStore.collapsed;

      if (!defaultCollapsed) {
        computedBounds.width = menuCollapsed
          ? bounds.width + LEFT_OFFSET
          : bounds.width;
      } else {
        computedBounds.width = menuCollapsed
          ? bounds.width - LEFT_OFFSET
          : bounds.width;
      }
    }
    return computedBounds;
  }, [MenuStore.collapsed, bounds, defaultCollapsed]);

  // TODO: improve me
  const { viewType } = store.getPreviewData;
  const content = useMemo(() => getContent(viewType), [viewType]);

  const realProps = {
    Nav: getNav,
    Content: content,
    options: { showNav: true },
    ...realBounds,
  };
  return (<div
    ref={rootRef}
    className="c7n-deployment-wrap"
  >
    <LayoutPage {...realProps} />
  </div>);
});

export default inject('MenuStore')(MainView);
