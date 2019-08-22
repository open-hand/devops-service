import React, { Fragment, lazy, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs, Spin } from 'choerodon-ui';
import { useEnvironmentStore } from './stores';
import { useResourceStore } from '../../../stores';
import StatusDot from '../../../../../components/status-dot';
import PrefixTitle from '../../components/prefix-title';
import Modals from './modals';

import './index.less';

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

  const title = useMemo(() => {
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
        <span className={`${prefixCls}-title-text`}>{name}</span>
      </Fragment>;
    }
    return null;
  }, [baseInfoDs.current]);

  return (
    <div className={`${prefixCls}-environment`}>
      <Modals />
      <PrefixTitle
        prefixCls={prefixCls}
        fallback={!title}
      >
        {title}
      </PrefixTitle>
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
    </div>
  );
});

export default EnvContent;
