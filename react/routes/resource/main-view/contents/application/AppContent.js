import React, { Fragment, lazy, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs, Icon } from 'choerodon-ui';
import { useApplicationStore } from './stores';
import { useResourceStore } from '../../../stores';
import PrefixTitle from '../../components/prefix-title';
import Modals from './modals';

import './index.less';

const { TabPane } = Tabs;

const MappingContent = lazy(() => import('./configs'));
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

  const title = useMemo(() => {
    const record = baseInfoDs.current;
    if (record) {
      const name = record.get('name');
      return <Fragment>
        <Icon type="widgets" />
        <span className={`${prefixCls}-title-text`}>{name}</span>
      </Fragment>;
    }
    return null;
  }, [baseInfoDs.current]);

  return (
    <div className={`${prefixCls}-application`}>
      <Modals />
      <PrefixTitle
        prefixCls={prefixCls}
        fallback={!title}
      >
        {title}
      </PrefixTitle>
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
          <Suspense fallback={<div>loading</div>}>
            <NetContent />
          </Suspense>
        </TabPane>
        <TabPane
          key={MAPPING_TAB}
          tab={formatMessage({ id: `${intlPrefix}.application.tabs.mapping` })}
        >
          <Suspense fallback={<div>loading</div>}>
            <MappingContent />
          </Suspense>
        </TabPane>
        <TabPane
          key={CIPHER_TAB}
          tab={formatMessage({ id: `${intlPrefix}.application.tabs.cipher` })}
        >
          <Suspense fallback={<div>loading</div>}>
            <MappingContent />
          </Suspense>
        </TabPane>
      </Tabs>
    </div>
  );
});

export default AppContent;
