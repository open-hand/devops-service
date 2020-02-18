import React, { useState, Fragment, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs } from 'choerodon-ui/pro';
import map from 'lodash/map';
import { Collapse } from 'choerodon-ui';
import { useClusterMainStore } from '../../../../stores';
import { useClusterContentStore } from '../../stores';
import Progress from '../components/Progress';

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

  const clusterData = useMemo(() => ([
    {
      name: '健康检查',
      code: 'health',
      value: 90,
    },
    {
      name: '镜像检查',
      code: 'mirror',
      value: 60,
    },
    {
      name: '网络配置',
      code: 'netword',
      value: 100,
    },
    {
      name: '资源分配',
      code: 'resource',
      value: 0,
    },
    {
      name: '安全',
      code: 'security',
      value: 50,
    },
  ]), []);

  function refresh() {

  }

  function getClusterHeader(item) {
    const { name, value } = item || {};
    return (
      <div className={`${prefixCls}-polaris-tabs-header`}>
        <span className={`${prefixCls}-polaris-mgl-10`}>{name}</span>
        <span className={`${prefixCls}-polaris-tabs-header-score`}>
          {formatMessage({ id: `${intlPrefix}.polarise.score` })}:
        </span>
        <span>{value}%</span>
        <Progress
          loading={loading}
          num={num}
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
            {formatMessage({ id: `${intlPrefix}.polarise.internal.${internal}` })}
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
            {map(clusterData, (item) => (
              <Panel header={getClusterHeader(item)} key={item.code}>
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
