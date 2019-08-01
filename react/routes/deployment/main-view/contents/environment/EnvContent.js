import React, { Fragment, useState, lazy, Suspense, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs } from 'choerodon-ui';
import { useEnvironmentStore } from './stores';
import { useDeploymentStore } from '../../../stores';
import StatusDot from '../../components/status-dot';
import PrefixTitle from '../../components/prefix-title';

import './index.less';

const { TabPane } = Tabs;
const SYNC_TAB = 'sync';
const ASSIGN_TAB = 'assign';

const SyncSituation = lazy(() => import('./sync-situation'));
const AssignPermissions = lazy(() => import('./assign-permissions'));

const EnvContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
  } = useDeploymentStore();
  const {
    intl: { formatMessage },
    baseInfoDs,
  } = useEnvironmentStore();

  const [activeKey, setActiveKey] = useState(SYNC_TAB);
  const handleChange = useCallback((key) => {
    setActiveKey(key);
  }, []);

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
      <PrefixTitle
        prefixCls={prefixCls}
        fallback={!baseInfo.length}
      >
        {title}
      </PrefixTitle>
      <Tabs
        animated={false}
        activeKey={activeKey}
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
            <AssignPermissions />
          </Suspense>
        </TabPane>
      </Tabs>
    </div>
  );
});

export default EnvContent;
