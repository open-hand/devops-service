import React, { Fragment, lazy, Suspense } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs, Spin } from 'choerodon-ui';
import { useEnvironmentStore } from '../../../stores';
import { useDetailStore } from './stores';
import StatusDot from '../../../../../components/status-dot';
import PageTitle from '../../../../../components/page-title';
// import Modals from './modals';

const { TabPane } = Tabs;

const SyncSituation = lazy(() => import('./sync'));
const Config = lazy(() => import('./DeployConfig'));
const Permissions = lazy(() => import('./Permissions'));

const EnvContent = observer(() => {
  const {
    envStore: {
      getSelectedMenu: {
        active,
        name,
        connect,
        synchro,
      },
    },
  } = useEnvironmentStore();
  const {
    intlPrefix,
    intl: { formatMessage },
    tabs: {
      SYNC_TAB,
      CONFIG_TAB,
      ASSIGN_TAB,
    },
    detailStore,
  } = useDetailStore();

  function handleChange(key) {
    detailStore.setTabKey(key);
  }

  return (
    <Fragment>
      <PageTitle>
        <StatusDot
          connect={connect}
          synchronize={synchro}
          active={active}
        />
        <span className="c7ncd-page-title-text">{name}</span>
      </PageTitle>
      <Tabs
        animated={false}
        activeKey={detailStore.getTabKey}
        onChange={handleChange}
      >
        <TabPane
          key={SYNC_TAB}
          tab={formatMessage({ id: `${intlPrefix}.environment.tabs.sync` })}
        >
          <Suspense fallback={<Spin />}>
            <SyncSituation />
          </Suspense>
        </TabPane>
        <TabPane
          key={CONFIG_TAB}
          tab={formatMessage({ id: `${intlPrefix}.environment.tabs.config` })}
        >
          <Suspense fallback={<Spin />}>
            <Config />
          </Suspense>
        </TabPane>
        <TabPane
          key={ASSIGN_TAB}
          tab={formatMessage({ id: `${intlPrefix}.environment.tabs.assignPermissions` })}
        >
          <Suspense fallback={<Spin />}>
            <Permissions />
          </Suspense>
        </TabPane>
      </Tabs>
      {/* <Modals /> */}
    </Fragment>
  );
});

export default EnvContent;
