import React, { Fragment, lazy, Suspense } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs, Spin } from 'choerodon-ui';
import { useEnvironmentStore } from './stores';
import { useResourceStore } from '../../../stores';
import StatusDot from '../../../../../components/status-dot';
import PageTitle from '../../../../../components/page-title';
import Modals from './modals';

const { TabPane } = Tabs;

const SyncSituation = lazy(() => import('./sync-situation'));
const Permissions = lazy(() => import('./Permissions'));

const EnvContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    treeDs,
  } = useResourceStore();
  const {
    intl: { formatMessage },
    baseInfoDs,
    tabs: {
      SYNC_TAB,
      ASSIGN_TAB,
    },
    envStore,
  } = useEnvironmentStore();

  function handleChange(key) {
    envStore.setTabKey(key);
  }

  function getTitle() {
    const record = baseInfoDs.current;
    if (record) {
      const id = record.get('id');
      const name = record.get('name');
      const connect = record.get('connect');
      const synchronize = record.get('synchronize');

      const menuItem = treeDs.find((item) => item.get('id') === id);
      if (menuItem
        && (menuItem.get('connect') !== connect
          || menuItem.get('synchronize') !== synchronize
          || menuItem.get('name') !== name)) {
        menuItem.set('connect', connect);
        menuItem.set('synchronize', synchronize);
        menuItem.set('name', name);
      }

      return <Fragment>
        <StatusDot
          connect={connect}
          synchronize={synchronize}
        />
        <span className="c7ncd-page-title-text">{name}</span>
      </Fragment>;
    }
    return null;
  }

  return (
    <div className={`${prefixCls}-environment`}>
      <PageTitle>
        {getTitle()}
      </PageTitle>
      <Tabs
        animated={false}
        activeKey={envStore.getTabKey}
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
        {envStore.getPermission && <TabPane
          key={ASSIGN_TAB}
          tab={formatMessage({ id: `${intlPrefix}.environment.tabs.assignPermissions` })}
        >
          <Suspense fallback={<Spin />}>
            <Permissions />
          </Suspense>
        </TabPane>}
      </Tabs>
      <Modals />
    </div>
  );
});

export default EnvContent;
