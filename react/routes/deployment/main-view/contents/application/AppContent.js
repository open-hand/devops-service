import React, { useContext, Fragment, useState, lazy, Suspense, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs, Icon } from 'choerodon-ui';
import { useApplicationStore } from './stores';
import { useDeploymentStore } from '../../../stores';
import PrefixTitle from '../../components/prefix-title';

import './index.less';

const { TabPane } = Tabs;
const NET_TAB = 'net';
const MAPPING_TAB = 'mapping';
const CIPHER_TAB = 'cipher';

const CipherContent = lazy(() => import('./cipher'));
const MappingContent = lazy(() => import('./mapping'));
const NetContent = lazy(() => import('./net'));

const AppContent = observer(() => {
  const {
    intl: { formatMessage },
    baseInfoDs,
  } = useApplicationStore();
  const {
    prefixCls,
    intlPrefix,
  } = useDeploymentStore();
  const [activeKey, setActiveKey] = useState(NET_TAB);
  const handleChange = useCallback((key) => {
    setActiveKey(key);
  }, []);

  const baseInfo = baseInfoDs.data;

  let title = null;
  if (baseInfo.length) {
    const record = baseInfo[0];
    const name = record.get('name');

    title = <Fragment>
      <Icon type="widgets" />
      <span className={`${prefixCls}-title-text`}>{name}</span>
    </Fragment>;
  }

  return (
    <div className={`${prefixCls}-application`}>
      <PrefixTitle
        prefixCls={prefixCls}
        fallback={!baseInfo.length}
      >
        {title}
      </PrefixTitle>
      <Tabs
        className={`${prefixCls}-application-tabs`}
        animated={false}
        activeKey={activeKey}
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
            <CipherContent />
          </Suspense>
        </TabPane>
      </Tabs>
    </div>
  );
});

export default AppContent;
