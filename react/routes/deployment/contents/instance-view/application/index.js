import React, { useContext, useMemo, useState, lazy, Suspense, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { DataSet } from 'choerodon-ui/pro';
import { Tabs } from 'choerodon-ui';
import BaseInfoDataSet from './stores/BaseInfoDataSet';
import Store from '../../../stores';
import { AppTitle } from '../../../components/prefix-title';

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
    selectedMenu: { menuId },
    intl: { formatMessage },
    prefixCls,
    intlPrefix,
    AppState: { currentMenuType: { id } },
  } = useContext(Store);
  const [activeKey, setActiveKey] = useState(NET_TAB);
  const handleChange = useCallback((key) => {
    setActiveKey(key);
  }, []);

  const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet(id, menuId)), [id, menuId]);
  const baseInfo = baseInfoDs.data;

  return (
    <div className={`${prefixCls}-application`}>
      <AppTitle records={baseInfo} prefixCls={prefixCls} />
      <Tabs
        className={`${prefixCls}-environment-tabs`}
        animated={false}
        activeKey={activeKey}
        onChange={handleChange}
      >
        <TabPane
          key={NET_TAB}
          tab={formatMessage({ id: `${intlPrefix}.instance.tabs.cases` })}
        >
          <Suspense fallback={<div>loading</div>}>
            <NetContent />
          </Suspense>
        </TabPane>
        <TabPane
          key={MAPPING_TAB}
          tab={formatMessage({ id: `${intlPrefix}.instance.tabs.cases` })}
        >
          <Suspense fallback={<div>loading</div>}>
            <MappingContent />
          </Suspense>
        </TabPane>
        <TabPane
          key={CIPHER_TAB}
          tab={formatMessage({ id: `${intlPrefix}.instance.tabs.cases` })}
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
