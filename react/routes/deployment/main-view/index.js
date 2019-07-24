import React, { useState, useRef, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import _ from 'lodash';
import LayoutPage from '../components/layout';
import Sidebar from '../sidebar';
import DeploymentStore from '../sidebar/stores';

import './index.less';

const LEFT_OFFSET = 200;

const getNav = ({ navBounds }) => <Sidebar navBounds={navBounds} />;

const MainView = observer(({ AppState: { currentMenuType }, MenuStore  }) => {
  const rootRef = useRef(null);
  const [bounds, setBounds] = useState(null);

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
    DeploymentStore.loadNavData(currentMenuType.id);
    getRootBounds();

    window.addEventListener('resize', getRootBounds, true);
    return () => {
      window.removeEventListener('resize', getRootBounds);
    };
  }, []);

  const realBounds = { ...bounds };
  if (bounds) {
    // 菜单栏展开关闭
    const menuCollapsed = MenuStore.collapsed;

    if (!MenuStore.collapsed) {
      realBounds.width = menuCollapsed
        ? bounds.width + LEFT_OFFSET
        : bounds.width;
    } else {
      realBounds.width = menuCollapsed
        ? bounds.width - LEFT_OFFSET
        : bounds.width;
    }
  }

  const realProps = {
    Nav: getNav,
    Content: () => <div>hello</div>,
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

export default inject('AppState', 'MenuStore')(MainView);
