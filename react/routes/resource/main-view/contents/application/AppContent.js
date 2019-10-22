import React, { Fragment, lazy, Suspense, memo, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs, Icon, Spin } from 'choerodon-ui';
import PageTitle from '../../../../../components/page-title';
import { useApplicationStore } from './stores';
import { useResourceStore } from '../../../stores';
import Modals from './modals';

import './index.less';

const { TabPane } = Tabs;

const Configs = lazy(() => import('./configs'));
const Cipher = lazy(() => import('./cipher'));
const NetContent = lazy(() => import('./net'));

const AppTitle = memo(({ name }) => (<Fragment>
  <Icon type="widgets" />
  <span className="c7ncd-page-title-text">{name}</span>
</Fragment>));

const AppContent = observer(() => {
  const {
    intl: { formatMessage },
    tabs: {
      NET_TAB,
      MAPPING_TAB,
      CIPHER_TAB,
    },
    baseInfoDs,
    appStore,
  } = useApplicationStore();
  const {
    prefixCls,
    intlPrefix,
    resourceStore,
    treeDs,
  } = useResourceStore();
  const { getSelectedMenu: { key: selectedKey } } = resourceStore;

  function handleChange(key) {
    appStore.setTabKey(key);
  }

  function getCurrent() {
    const record = baseInfoDs.current;
    if (record) {
      const id = record.get('id');
      const name = record.get('name');
      return { id, name };
    }
    return null;
  }

  useEffect(() => {
    const current = getCurrent();
    if (current) {
      const menuItem = treeDs.find((item) => item.get('key') === selectedKey && item.get('id') === current.id);

      if (menuItem && menuItem.get('name') !== current.name) {
        menuItem.set('name', current.name);
      }
    }
  });

  function getTitle() {
    const current = getCurrent();
    if (current) {
      return <AppTitle name={current.name} />;
    }
    return null;
  }

  function getFallBack() {
    const { name } = resourceStore.getSelectedMenu;
    return <AppTitle name={name} />;
  }

  return (
    <div className={`${prefixCls}-application`}>
      <PageTitle fallback={getFallBack()} content={getTitle()} />
      <Tabs
        className={`${prefixCls}-application-tabs`}
        animated={false}
        activeKey={appStore.getTabKey}
        onChange={handleChange}
      >
        <TabPane
          key={NET_TAB}
          tab={formatMessage({ id: `${intlPrefix}.application.tabs.networking` })}
        >
          <Suspense fallback={<Spin />}>
            <NetContent />
          </Suspense>
        </TabPane>
        <TabPane
          key={MAPPING_TAB}
          tab={formatMessage({ id: `${intlPrefix}.application.tabs.mapping` })}
        >
          <Suspense fallback={<Spin />}>
            <Configs />
          </Suspense>
        </TabPane>
        <TabPane
          key={CIPHER_TAB}
          tab={formatMessage({ id: `${intlPrefix}.application.tabs.cipher` })}
        >
          <Suspense fallback={<Spin />}>
            <Cipher />
          </Suspense>
        </TabPane>
      </Tabs>
      <Modals />
    </div>
  );
});

export default AppContent;
