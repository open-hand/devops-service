import React, { useContext, useMemo, useState, lazy, Suspense, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { DataSet } from 'choerodon-ui/pro';
import { Tabs } from 'choerodon-ui';
import BaseInfoDataSet from './stores/BaseInfoDataSet';
import Store from '../../../stores';
import { IstTitle } from '../../../components/prefix-title';

import './index.less';

const { TabPane } = Tabs;
const CASES_TAB = 'cases';
const DETAILS_TAB = 'details';
const PODS_TAB = 'pods';

const Cases = lazy(() => import('./cases'));
const Details = lazy(() => import('./details'));
const PodsDetails = lazy(() => import('./pods-details'));

const InstanceContent = observer(() => {
  const {
    selectedMenu: { menuId },
    intl: { formatMessage },
    prefixCls,
    intlPrefix,
    AppState: { currentMenuType: { id } },
  } = useContext(Store);
  const [activeKey, setActiveKey] = useState(CASES_TAB);
  const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet(id, menuId)), [id, menuId]);
  const baseInfo = baseInfoDs.data;

  const handleChange = useCallback((key) => {
    setActiveKey(key);
  }, []);

  return (
    <div className={`${prefixCls}-instance`}>
      <IstTitle prefixCls={prefixCls} records={baseInfo} />
      <Tabs
        className={`${prefixCls}-environment-tabs`}
        animated={false}
        activeKey={activeKey}
        onChange={handleChange}
      >
        <TabPane
          key={CASES_TAB}
          tab={formatMessage({ id: `${intlPrefix}.instance.tabs.cases` })}
        >
          <Suspense fallback={<div>loading</div>}>
            <Cases />
          </Suspense>
        </TabPane>
        <TabPane
          key={DETAILS_TAB}
          tab={formatMessage({ id: `${intlPrefix}.instance.tabs.details` })}
        >
          <Suspense fallback={<div>loading</div>}>
            <Details />
          </Suspense>
        </TabPane>
        <TabPane
          key={PODS_TAB}
          tab={formatMessage({ id: `${intlPrefix}.instance.tabs.pods` })}
        >
          <Suspense fallback={<div>loading</div>}>
            <PodsDetails />
          </Suspense>
        </TabPane>
      </Tabs>
    </div>
  );
});

export default InstanceContent;
