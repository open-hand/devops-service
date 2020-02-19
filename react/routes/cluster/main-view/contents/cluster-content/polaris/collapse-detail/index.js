import React, { useState, Fragment, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs } from 'choerodon-ui/pro';
import map from 'lodash/map';
import { Collapse, Progress, Icon } from 'choerodon-ui';
import { useClusterMainStore } from '../../../../stores';
import { useClusterContentStore } from '../../stores';
import ProgressBar from '../components/Progress';

import './index.less';

const { TabPane } = Tabs;
const { Panel } = Collapse;

const collapseDetail = observer((props) => {
  const {
    intlPrefix,
    prefixCls,
  } = useClusterMainStore();
  const {
    contentStore: {
      setTabKey,
    },
    formatMessage,
    tabs: {
      POLARIS_TAB,
    },
    ClusterDetailDs,
    clusterSummaryDs,
    envDetailDs,
  } = useClusterContentStore();

  const [loading, setLoading] = useState(false);
  const [num, setNum] = useState(80);

  const envs = useMemo(() => ([
    {
      name: 'Staging环境',
      code: 'staging',
      projectName: 'choerodon',
      internal: true,
    },
    {
      name: 'Uat环境',
      code: 'uat',
      projectName: 'choerodon',
      internal: true,
    },
    {
      name: '生产环境',
      code: 'produce',
      projectName: 'choerodon',
      internal: false,
    },
  ]), []);

  const clusterSummary = useMemo(() => (['healthCheck', 'imageCheck', 'networkCheck', 'resourceCheck', 'securityCheck']), []);

  function refresh() {

  }

  function getClusterHeader(item) {
    const { score, hasErrors } = clusterSummaryDs.current ? (clusterSummaryDs.current.get(item) || {}) : {};
    const isLoading = clusterSummaryDs.status === 'loading' || loading;
    return (
      <div className={`${prefixCls}-polaris-tabs-header`}>
        <span className={`${prefixCls}-polaris-mgl-10`}>
          {formatMessage({ id: `${intlPrefix}.polaris.${item}` })}
        </span>
        <span className={`${prefixCls}-polaris-tabs-header-score`}>
          {formatMessage({ id: `${intlPrefix}.polaris.score` })}:
        </span>
        {isLoading ? <Progress type="loading" size="small" /> : <span>{score}%</span>}
        {hasErrors && <Icon type="cancel" />}
        <ProgressBar
          loading={isLoading}
          num={isLoading ? 0 : score}
        />
      </div>
    );
  }

  function getEnvHeader(env) {
    const { name, code, projectName, internal } = env || {};
    return (
      <div className={`${prefixCls}-polaris-tabs-header`}>
        <div className={`${prefixCls}-polaris-tabs-header-item`}>
          <span className={`${prefixCls}-polaris-tabs-header-env-${internal}`}>
            {formatMessage({ id: `${intlPrefix}.polaris.internal.${internal}` })}
          </span>
        </div>
        {code && (
          <div className={`${prefixCls}-polaris-tabs-header-item`}>
            <span className={`${prefixCls}-polaris-tabs-header-text`}>{formatMessage({ id: 'environment' })}:</span>
            <span>{name}</span>
          </div>
        )}
        <div className={`${prefixCls}-polaris-tabs-header-item`}>
          <span className={`${prefixCls}-polaris-tabs-header-text`}>{formatMessage({ id: 'envCode' })}:</span>
          <span>{code}</span>
        </div>
        {projectName && (
          <div className={`${prefixCls}-polaris-tabs-header-item`}>
            <span className={`${prefixCls}-polaris-tabs-header-text`}>{formatMessage({ id: `${intlPrefix}.belong.project` })}:</span>
            <span>{projectName}</span>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className={`${prefixCls}-polaris-wrap-number`}>
      <Tabs
        type="card"
        tabBarGutter={0}
        className={`${prefixCls}-polaris-tabs`}
      >
        <TabPane
          tab={formatMessage({ id: `${intlPrefix}.polaris.cluster` })}
          className={`${prefixCls}-polaris-tabs-item`}
          key="cluster"
        >
          <Collapse bordered={false} defaultActiveKey={['1']}>
            {map(clusterSummary, (item) => (
              <Panel header={getClusterHeader(item)} key={item}>
                content
              </Panel>
            ))}
          </Collapse>
        </TabPane>
        <TabPane
          tab={formatMessage({ id: `${intlPrefix}.polaris.env` })}
          key="environment"
        >
          <Collapse bordered={false} defaultActiveKey={['1']}>
            {map(envs, (env) => (
              <Panel header={getEnvHeader(env)} key={env.code}>
                content
              </Panel>
            ))}
          </Collapse>
        </TabPane>
      </Tabs>
    </div>
  );
});

export default collapseDetail;
