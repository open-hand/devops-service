import React, { Fragment, lazy, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs, Spin } from 'choerodon-ui';
import { useClusterContentStore } from './stores';
import Modals from './modals';


const { TabPane } = Tabs;

const NodeList = lazy(() => import('./node-list'));
const PermissionList = lazy(() => import('./permission-list'));

export default observer((props) => {
  const {
    intlPrefix,
    intl: { formatMessage },
    tabs: {
      NODE_TAB,
      ASSIGN_TAB,
    },
    contentStore,
  } = useClusterContentStore();
  const handleChange = (key) => {
    contentStore.setTabKey(key);
  };

  return (
    <div>
      <Modals />
      <Tabs
        animated={false}
        activeKey={contentStore.getTabKey}
        onChange={handleChange}
      >
        <TabPane
          key={NODE_TAB}
          tab={formatMessage({ id: `${intlPrefix}.node.list` })}
        >
          <Suspense fallback={<Spin />}>
            <NodeList />
          </Suspense>
        </TabPane>
        <TabPane
          key={ASSIGN_TAB}
          tab={formatMessage({ id: `${intlPrefix}.permission.assign` })}
        >
          <Suspense fallback={<Spin />}>
            <PermissionList />
          </Suspense>
        </TabPane>
      </Tabs>
    </div>);
});
