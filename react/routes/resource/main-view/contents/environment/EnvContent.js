import React, { Fragment, lazy, Suspense } from 'react';
import { observer } from 'mobx-react-lite';
import { Permission } from '@choerodon/master';
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
      const name = record.get('name');
      const connect = record.get('connect');
      const synchronize = record.get('synchronize');

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
        <TabPane
          key={ASSIGN_TAB}
          tab={formatMessage({ id: `${intlPrefix}.environment.tabs.assignPermissions` })}
        >
          <Suspense fallback={<Spin />}>
            <Permissions />
          </Suspense>
        </TabPane>
      </Tabs>
      <Modals />
    </div>
  );
});

export default EnvContent;
