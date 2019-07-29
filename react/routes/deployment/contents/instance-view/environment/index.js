import React, { useContext, useMemo, useState, lazy, Suspense, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { DataSet } from 'choerodon-ui/pro';
import { Tabs } from 'choerodon-ui';
import BaseInfoDataSet from './stores/BaseInfoDataSet';
import Store from '../../../stores';
import { EnvTitle } from '../../../components/prefix-title';

import './index.less';

const { TabPane } = Tabs;
const SYNC_TAB = 'sync';
const ASSIGN_TAB = 'assign';

const AssignPermissions = lazy(() => import('./assign-permissions'));
const SyncSituation = lazy(() => import('./sync-situation'));

const EnvContent = observer(() => {
  const {
    selectedMenu: { menuId },
    intl: { formatMessage },
    prefixCls,
    intlPrefix,
    AppState: { currentMenuType: { id } },
  } = useContext(Store);
  const [activeKey, setActiveKey] = useState(SYNC_TAB);
  const handleChange = useCallback((key) => {
    setActiveKey(key);
  }, []);

  const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet(id, menuId)), [id, menuId]);
  const baseInfo = baseInfoDs.data;

  return (
    <div className={`${prefixCls}-environment`}>
      <EnvTitle
        prefixCls={prefixCls}
        records={baseInfo}
      />
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
