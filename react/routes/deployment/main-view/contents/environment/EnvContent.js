import React, { Fragment, lazy, Suspense, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs } from 'choerodon-ui';
import { useEnvironmentStore } from './stores';
import { useDeploymentStore } from '../../../stores';
import StatusDot from '../../components/status-dot';
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
  } = useDeploymentStore();
  const {
    intl: { formatMessage },
    baseInfoDs,
    tabs: {
      SYNC_TAB,
      ASSIGN_TAB,
    },
    envStore,
  } = useEnvironmentStore();

  const handleChange = useCallback((key) => {
    envStore.setTabKey(key);
  }, [envStore]);

  const baseInfo = baseInfoDs.data;

  let title = null;
  if (baseInfo.length) {
    const record = baseInfo[0];
    const name = record.get('name');
    const connect = record.get('connect');
    const synchronize = record.get('synchronize');

    title = <Fragment>
      <StatusDot
        connect={connect}
        synchronize={synchronize}
      />
      <span className={`${prefixCls}-title-text`}>{name}</span>
    </Fragment>;
  }

  return (
    <div className={`${prefixCls}-environment`}>
      <Modals />
      <PrefixTitle
        prefixCls={prefixCls}
        fallback={!baseInfo.length}
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
          <Suspense fallback={<div>loading</div>}>
            <SyncSituation />
          </Suspense>
        </TabPane>
        <TabPane
          key={ASSIGN_TAB}
          tab={formatMessage({ id: `${intlPrefix}.environment.tabs.assignPermissions` })}
        >
          <Suspense fallback={<div>loading</div>}>
            <Permissions />
          </Suspense>
        </TabPane>
      </Tabs>
    </div>
  );
});

export default EnvContent;
