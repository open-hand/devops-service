import React, { lazy, Suspense } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs, Spin, message } from 'choerodon-ui';
import { useEnvironmentStore } from './stores';
import { useResourceStore } from '../../../stores';
import PageTitle from '../../../../../components/page-title';
import EnvItem from '../../../../../components/env-item';
import openWarnModal from '../../../../../utils/openWarnModal';
import Modals from './modals';

const { TabPane } = Tabs;

const SyncSituation = lazy(() => import('./sync-situation'));
const Permissions = lazy(() => import('./Permissions'));

const EnvContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    treeDs,
    resourceStore: { getSelectedMenu },
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

  function refresh() {
    treeDs.query();
  }

  function getTitle() {
    const record = baseInfoDs.current;
    if (record) {
      const id = record.get('id');
      const name = record.get('name');
      const active = record.get('active');
      const connect = record.get('connect');
      const menuItem = treeDs.find((item) => item.get('id') === id);

      if (menuItem) {
        // 清除已经停用的环境
        if (!active) {
          openWarnModal(refresh, formatMessage);
        } else if ((menuItem.get('connect') !== connect
          || menuItem.get('name') !== name)) {
          menuItem.set('connect', connect);
          menuItem.set('name', name);
          message.info('基本数据发生变化，已更新。');
        }
      }

      return <EnvItem isTitle name={name} connect={connect} />;
    }
    return null;
  }

  function getFallBack() {
    const {
      name,
      connect,
    } = getSelectedMenu;
    return <EnvItem isTitle name={name} connect={connect} />;
  }

  return (
    <div className={`${prefixCls}-environment`}>
      <PageTitle fallback={getFallBack()} content={getTitle()} />
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
