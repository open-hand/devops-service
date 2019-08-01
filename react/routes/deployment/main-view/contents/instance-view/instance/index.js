import React, { useContext, useMemo, useState, lazy, Suspense, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { DataSet } from 'choerodon-ui/pro';
import { Tabs } from 'choerodon-ui';
import BaseInfoDataSet from './stores/BaseInfoDataSet';
import Store from '../../../../stores';
import PrefixTitle from '../../../components/prefix-title';
import PodCircle from '../../../components/pod-circle';

import './index.less';

const RUNNING_COLOR = '#0bc2a8';
const PADDING_COLOR = '#fbb100';

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

  // const record = records[0];
  // const name = record.get('code');
  // const podRunningCount = record.get('podRunningCount');
  // const podCount = record.get('podCount');
  // const podUnlinkCount = podCount - podRunningCount;
  //
  // return <TitleWrap prefixCls={prefixCls}>
  //   <PodCircle
  //     style={{
  //       width: 22,
  //       height: 22,
  //     }}
  //     dataSource={[{
  //       name: 'running',
  //       value: podRunningCount,
  //       stroke: RUNNING_COLOR,
  //     }, {
  //       name: 'unlink',
  //       value: podUnlinkCount,
  //       stroke: PADDING_COLOR,
  //     }]}
  //   />
  //   <span className={`${prefixCls}-title-text`}>{name}</span>
  // </TitleWrap>;

  return (
    <div className={`${prefixCls}-instance`}>

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
