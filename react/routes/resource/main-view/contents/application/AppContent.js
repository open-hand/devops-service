import React, { Fragment, lazy, Suspense } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs, Icon, Spin } from 'choerodon-ui';
import { useApplicationStore } from './stores';
import { useResourceStore } from '../../../stores';
import PageTitle from '../../../../../components/page-title';
import Modals from './modals';

import './index.less';

const { TabPane } = Tabs;

const Configs = lazy(() => import('./configs'));
const Cipher = lazy(() => import('./cipher'));
const NetContent = lazy(() => import('./net'));

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
  } = useResourceStore();

  function handleChange(key) {
    appStore.setTabKey(key);
  }

  function getTitle() {
    const record = baseInfoDs.current;
    if (record) {
      const name = record.get('name');
      return <Fragment>
        <Icon type="widgets" />
        <span className="c7ncd-page-title-text">{name}</span>
      </Fragment>;
    }
    return null;
  }
  return (
    <div className={`${prefixCls}-application`}>
      <PageTitle>
        {getTitle()}
      </PageTitle>
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
