import React, { Fragment, useContext, useMemo, useState, lazy, Suspense, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { DataSet } from 'choerodon-ui/pro';
import { Tabs } from 'choerodon-ui';
import BaseInfoDataSet from './stores/BaseInfoDataSet';
import Store from '../../../stores';
import PodCircle from '../../../components/pod-circle';

import './index.less';

const { TabPane } = Tabs;
const CASES_TAB = 'cases';
const DETAILS_TAB = 'details';
const PODS_TAB = 'pods';

const RUNNING_COLOR = '#0bc2a8';
const PADDING_COLOR = '#fbb100';

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

  const getTitle = useMemo(() => {
    if (baseInfo.length) {
      const record = baseInfo[0];
      const name = record.get('name');
      const podRunningCount = record.get('podRunningCount');
      const podCount = record.get('podCount');
      const podUnlinkCount = podCount - podRunningCount;

      return <Fragment>
        <PodCircle
          size="small"
          dataSource={[{
            name: 'running',
            value: podRunningCount,
            stroke: RUNNING_COLOR,
          }, {
            name: 'unlink',
            value: podUnlinkCount,
            stroke: PADDING_COLOR,
          }]}
        />
        <span className={`${prefixCls}-environment-title`}>{name}</span>
      </Fragment>;
    }
    return null;
  }, [baseInfo, prefixCls]);
  const handleChange = useCallback((key) => {
    setActiveKey(key);
  }, []);

  // const getPanes = useMemo(() => {
  //   const cmMap = {
  //     [SYNC_TAB]: <SyncSituation />,
  //     [ASSIGN_TAB]: <AssignPermissions />,
  //   };
  //
  //   return <Suspense fallback={<div>loading</div>}>
  //     {cmMap[activeKey]}
  //   </Suspense>;
  // }, [activeKey]);

  return (
    <div className={`${prefixCls}-environment`}>
      <div className={`${prefixCls}-environment-info`}>
        {getTitle}
      </div>

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
      </Tabs>
      <TabPane
        key={PODS_TAB}
        tab={formatMessage({ id: `${intlPrefix}.instance.tabs.pods` })}
      >
        <Suspense fallback={<div>loading</div>}>
          <PodsDetails />
        </Suspense>
      </TabPane>
    </div>
  );
});

export default InstanceContent;
