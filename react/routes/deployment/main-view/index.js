import React, { useContext, useState, useRef, useEffect, useMemo, lazy, Suspense } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import _ from 'lodash';
import LayoutPage from '../components/layout';
import { IST_ITEM, APP_ITEM, ENV_ITEM } from '../Constants';
import Sidebar from '../sidebar';
import Store from '../stores';

import './index.less';

// 此处非Site层菜单的宽度，而是折叠态和非折叠态的宽度差
const LEFT_OFFSET = 200;
const getNav = ({ navBounds }) => <Sidebar navBounds={navBounds} />;

// 实例视图
const EnvContent = lazy(() => import('../contents/instance-view/environment'));
const AppContent = lazy(() => import('../contents/instance-view/application'));
const IstContent = lazy(() => import('../contents/instance-view/instance'));

const getContent = (type) => {
  const cmMaps = {
    [ENV_ITEM]: <EnvContent />,
    [APP_ITEM]: <AppContent />,
    [IST_ITEM]: <IstContent />,
  };

  return cmMaps[type]
    ? () => <Suspense fallback={<div>loading</div>}>{cmMaps[type]}</Suspense>
    : () => <div>加载数据中</div>;
};

const MainView = observer(({ MenuStore }) => {
  const { selectedMenu: { menuType } } = useContext(Store);

  const rootRef = useRef(null);
  const [bounds, setBounds] = useState(null);
  const [defaultCollapsed] = useState(() => MenuStore.collapsed);

  useEffect(() => {
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

  const content = useMemo(() => getContent(menuType), [menuType]);

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
