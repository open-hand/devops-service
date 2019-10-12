import React, { Fragment, lazy, Suspense, useEffect } from 'react';
import { runInAction } from 'mobx';
import { observer } from 'mobx-react-lite';
import pick from 'lodash/pick';
import { Tabs, Spin } from 'choerodon-ui';
import isEqual from 'lodash/isEqual';
import { useEnvironmentStore } from '../../../stores';
import { useDetailStore } from './stores';
import PageTitle from '../../../../../components/page-title';
import EnvItem from '../../../../../components/env-item';
import Modals from './modals';
import Tips from '../../../../../components/new-tips';

const { TabPane } = Tabs;

const SyncSituation = lazy(() => import('./sync'));
const Config = lazy(() => import('./DeployConfig'));
const Permissions = lazy(() => import('./Permissions'));

const EnvContent = observer(() => {
  const {
    envStore,
    treeDs,
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
    baseDs,
  } = useDetailStore();

  function handleChange(key) {
    detailStore.setTabKey(key);
  }

  function getCurrent() {
    const record = baseDs.current;
    if (record) {
      const id = record.get('id');
      const name = record.get('name');
      const active = record.get('active');
      const connect = record.get('connect');
      const synchro = record.get('synchronize');
      const failed = record.get('failed');

      return {
        id,
        name,
        active,
        connect,
        synchro,
        failed: failed || false,
      };
    }

    return null;
  }

  useEffect(() => {
    const update = getCurrent();
    if (update) {
      const menuItem = treeDs.find((item) => item.get('id') === update.id);
      if (menuItem) {
        const previous = pick(menuItem.toData(), ['id', 'active', 'name', 'connect', 'synchro', 'failed']);
        if (!isEqual(previous, update)) {
          runInAction(() => {
            menuItem.set(update);
            const newState = {
              ...envStore.getSelectedMenu,
              ...update,
            };
            envStore.setSelectedMenu(newState);
          });
        }
      }
    }
  });

  function getTitle() {
    const current = getCurrent();
    if (current) {
      return <EnvItem isTitle {...current} />;
    }
    return null;
  }

  function getFallBack() {
    const {
      name,
      connect,
      active,
    } = envStore.getSelectedMenu;
    return <EnvItem
      isTitle
      name={name}
      connect={connect}
      active={active}
    />;
  }

  return (
    <Fragment>
      <PageTitle
        content={getTitle()}
        fallback={getFallBack()}
      />
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
          tab={
            <Tips
              helpText={formatMessage({ id: `${intlPrefix}.permission.tab.tips` })}
              title={formatMessage({ id: `${intlPrefix}.environment.tabs.assignPermissions` })}
            />
          }
        >
          <Suspense fallback={<Spin />}>
            <Permissions />
          </Suspense>
        </TabPane>
      </Tabs>
      <Modals />
    </Fragment>
  );
});

export default EnvContent;
