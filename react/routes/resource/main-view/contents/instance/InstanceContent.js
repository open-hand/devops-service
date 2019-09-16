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

const InstanceContent = observer(() => {
  const podSize = useMemo(() => ({
    width: 22,
    height: 22,
  }), []);
  const {
    prefixCls,
    intlPrefix,
  } = useResourceStore();
  const {
    podColor: {
      RUNNING_COLOR,
      PADDING_COLOR,
    },
  } = useMainStore();
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
      const code = record.get('code');
      const podRunningCount = record.get('podRunningCount');
      const podCount = record.get('podCount');
      const podUnlinkCount = podCount - podRunningCount;
      const status = record.get('status');
      const commandType = record.get('commandType');
      const commandVersionId = record.get('commandVersionId');
      const commandVersion = record.get('commandVersion');
      const appServiceVersionId = record.get('appServiceVersionId');

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
        <span className="c7ncd-page-title-text">{code}</span>
        {commandType === 'update' && status === 'failed' && appServiceVersionId && commandVersionId && commandVersionId !== appServiceVersionId && (
          <Tooltip title={formatMessage({ id: `${intlPrefix}.instance.version.failed` }, { text: commandVersion })}>
            <Icon type="error" className={`${prefixCls}-instance-page-title-icon`} />
          </Tooltip>
        )}
      </Fragment>;
    }
    return null;
  }

  return (
    <div className={`${prefixCls}-instance`}>
      <PageTitle>
        {getTitle()}
      </PageTitle>
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
