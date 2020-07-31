import React, { Fragment, lazy, Suspense, useEffect, useMemo, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs, Spin } from 'choerodon-ui';
import { axios } from '@choerodon/boot';
import { useClusterContentStore } from './stores';
import Modals from './modals';
import PageTitle from '../../../../../components/page-title';
import StatusDot from '../../../../../components/status-dot';
import Tips from '../../../../../components/new-tips';

import './index.less';

const { TabPane } = Tabs;

const NodeList = lazy(() => import('./node-list'));
const PermissionList = lazy(() => import('./permission-list'));
const Monitor = lazy(() => import('./monitor'));
const ComponentManage = lazy(() => import('./component-manage'));
const Polaris = lazy(() => import('./polaris'));

export default observer((props) => {
  const {
    intlPrefix,
    intl: { formatMessage },
    tabs: {
      NODE_TAB,
      POLARIS_TAB,
      ASSIGN_TAB,
      COMPONENT_TAB,
      MONITOR_TAB,
    },
    contentStore,
    ClusterDetailDs,
  } = useClusterContentStore();
  const handleChange = (key) => {
    contentStore.setTabKey(key);
  };

  function title() {
    const record = ClusterDetailDs.current;
    if (record) {
      const name = record.get('name');
      const getStatus = () => {
        const connect = record.get('connect');
        if (connect) {
          return ['running', 'connect'];
        }
        return ['disconnect'];
      };

      return <Fragment>
        <StatusDot
          getStatus={getStatus}
        />
        <span className="c7ncd-page-title-text">{name}</span>
      </Fragment>;
    }
    return null;
  }

  return (
    <Fragment>
      <Modals />
      <PageTitle content={title()} />
      <Tabs
        animated={false}
        activeKey={contentStore.getTabKey}
        onChange={handleChange}
        className="c7ncd-cluster-tab-content"
      >
        <TabPane
          key={NODE_TAB}
          tab={formatMessage({ id: `${intlPrefix}.node.list` })}
        >
          <Suspense fallback={<Spin />}>
            <div className="c7ncd-cluster-node-list">
              <NodeList />
            </div>
          </Suspense>
        </TabPane>
        <TabPane
          key={POLARIS_TAB}
          tab={formatMessage({ id: `${intlPrefix}.polaris` })}
        >
          <Suspense fallback={<Spin />}>
            <Polaris />
          </Suspense>
        </TabPane>
        <TabPane
          key={ASSIGN_TAB}
          tab={<Tips
            helpText={formatMessage({ id: `${intlPrefix}.permission.tab.tips` })}
            title={formatMessage({ id: `${intlPrefix}.permission.assign` })}
          />}
        >
          <Suspense fallback={<Spin />}>
            <PermissionList />
          </Suspense>
        </TabPane>
        <TabPane
          key={COMPONENT_TAB}
          tab={formatMessage({ id: `${intlPrefix}.component` })}
        >
          <Suspense fallback={<Spin />}>
            <ComponentManage />
          </Suspense>
        </TabPane>
        <TabPane
          key={MONITOR_TAB}
          tab={formatMessage({ id: `${intlPrefix}.monitor` })}
        >
          <Suspense fallback={<Spin />}>
            <Monitor />
          </Suspense>
        </TabPane>

      </Tabs>
    </Fragment>);
});
