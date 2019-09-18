import React, { Fragment, lazy, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs, Tooltip, Icon, Spin } from 'choerodon-ui';
import PageTitle from '../../../../../components/page-title';
import PodCircle from '../../components/pod-circle';
import Modals from './modals';
import { useResourceStore } from '../../../stores';
import { useMainStore } from '../../stores';
import { useInstanceStore } from './stores';

import './index.less';

const { TabPane } = Tabs;

const Cases = lazy(() => import('./cases'));
const Details = lazy(() => import('./details'));
const PodsDetails = lazy(() => import('./pods-details'));

const InstanceTitle = ({
  podRunningCount,
  podUnlinkCount,
  status,
  name,
  errorText,
}) => {
  const podSize = useMemo(() => ({
    width: 22,
    height: 22,
  }), []);
  const { prefixCls } = useResourceStore();
  const {
    podColor: {
      RUNNING_COLOR,
      PADDING_COLOR,
    },
  } = useMainStore();

  return <Fragment>
    <PodCircle
      style={podSize}
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
    <span className="c7ncd-page-title-text">{name}</span>
    {status === 'failed' && (
      <Tooltip title={errorText}>
        <Icon type="error" className={`${prefixCls}-instance-page-title-icon`} />
      </Tooltip>
    )}
  </Fragment>;
};

const InstanceContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu },
  } = useResourceStore();
  const {
    intl: { formatMessage },
    tabs: {
      CASES_TAB,
      DETAILS_TAB,
      PODS_TAB,
    },
    istStore,
    baseDs,
  } = useInstanceStore();

  function handleChange(key) {
    istStore.setTabKey(key);
  }

  function getTitle() {
    const record = baseDs.current;
    if (record) {
      const podRunningCount = record.get('podRunningCount');
      const podCount = record.get('podCount');
      const podUnlinkCount = podCount - podRunningCount;

      return <InstanceTitle
        status={record.get('status')}
        name={record.get('code')}
        podRunningCount={podRunningCount}
        podUnlinkCount={podUnlinkCount}
        errorText={record.get('error')}
      />;
    }
    return null;
  }

  function getFallBack() {
    const {
      name,
      podRunningCount,
      podCount,
    } = getSelectedMenu;

    return <InstanceTitle name={name} podRunningCount={podRunningCount} podUnlinkCount={podCount - podRunningCount} />;
  }

  return (
    <div className={`${prefixCls}-instance`}>
      <PageTitle content={getTitle()} fallback={getFallBack()} />
      <Tabs
        className={`${prefixCls}-environment-tabs`}
        animated={false}
        activeKey={istStore.getTabKey}
        onChange={handleChange}
      >
        <TabPane
          key={CASES_TAB}
          tab={formatMessage({ id: `${intlPrefix}.instance.tabs.cases` })}
        >
          <Suspense fallback={<Spin />}>
            <Cases />
          </Suspense>
        </TabPane>
        <TabPane
          key={DETAILS_TAB}
          tab={formatMessage({ id: `${intlPrefix}.instance.tabs.details` })}
        >
          <Suspense fallback={<Spin />}>
            <Details />
          </Suspense>
        </TabPane>
        <TabPane
          key={PODS_TAB}
          tab={formatMessage({ id: `${intlPrefix}.instance.tabs.pods` })}
        >
          <Suspense fallback={<Spin />}>
            <PodsDetails />
          </Suspense>
        </TabPane>
      </Tabs>
      <Modals />
    </div>
  );
});

export default InstanceContent;
