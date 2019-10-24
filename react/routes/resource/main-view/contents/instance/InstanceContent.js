import React, { Fragment, lazy, Suspense, useEffect, useMemo } from 'react';
import { runInAction } from 'mobx';
import { observer } from 'mobx-react-lite';
import { Tabs, Tooltip, Icon, Spin, Progress } from 'choerodon-ui';
import isEqual from 'lodash/isEqual';
import pick from 'lodash/pick';
import omit from 'lodash/omit';
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
      <Tooltip
        title={errorText}
        placement="bottom"
        overlayClassName={`${prefixCls}-instance-page-title-error-tooltip`}
      >
        <Icon type="error" className={`${prefixCls}-instance-page-title-icon`} />
      </Tooltip>
    )}
    {status === 'operating' && (
      <Tooltip title="处理中">
        <Progress
          className={`${prefixCls}-instance-page-title-icon-loading`}
          type="loading"
          size="small"
        />
      </Tooltip>
    )}
  </Fragment>;
};

const InstanceContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore,
    treeDs,
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

  const { getSelectedMenu: { key: selectedKey } } = resourceStore;


  function handleChange(key) {
    istStore.setTabKey(key);
  }

  function getCurrent() {
    const record = baseDs.current;
    if (record) {
      const id = record.get('id');
      const status = record.get('status');
      const name = record.get('code');
      const podRunningCount = record.get('podRunningCount');
      const podCount = record.get('podCount');
      const error = record.get('error');
      return {
        id,
        status,
        name,
        podRunningCount,
        podCount,
        error,
      };
    }

    return null;
  }

  useEffect(() => {
    const current = getCurrent();
    if (current) {
      const menuItem = treeDs.find((item) => item.get('key') === selectedKey && item.get('id') === current.id);
      if (menuItem) {
        const previous = pick(menuItem.toData(), ['status', 'name', 'podRunningCount', 'podCount']);
        const next = omit(current, ['id', 'error']);

        if (!isEqual(previous, next)) {
          runInAction(() => {
            menuItem.set(next);
            resourceStore.setSelectedMenu({
              ...resourceStore.getSelectedMenu,
              ...next,
            });
          });
        }
      }
    }
  });

  function getTitle() {
    const current = getCurrent();
    if (current) {
      const {
        status,
        name,
        podRunningCount,
        podCount,
        error,
      } = current;
      const podUnlinkCount = computeUnlinkPod(podCount, podRunningCount);
      return <InstanceTitle
        status={status}
        name={name}
        podRunningCount={podRunningCount}
        podUnlinkCount={podUnlinkCount}
        errorText={error}
      />;
    }
    return null;
  }

  function getFallBack() {
    const {
      name,
      podRunningCount,
      podCount,
    } = resourceStore.getSelectedMenu;
    const podUnlinkCount = computeUnlinkPod(podCount, podRunningCount);

    return <InstanceTitle
      name={name}
      podRunningCount={podRunningCount}
      podUnlinkCount={podUnlinkCount}
    />;
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

function computeUnlinkPod(all, run) {
  return all - run;
}

export default InstanceContent;
