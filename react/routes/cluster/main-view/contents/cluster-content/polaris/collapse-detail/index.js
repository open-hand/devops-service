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
    polarisNumDS,
  } = useClusterContentStore();

  const [num, setNum] = useState(80);

  const clusterSummary = useMemo(() => (['healthCheck', 'imageCheck', 'networkCheck', 'resourceCheck', 'securityCheck']), []);
  const clusterSummaryData = useMemo(() => {
    if (clusterSummaryDs.current) {
      return clusterSummaryDs.current.toData();
    }
    return {};
  }, [clusterSummaryDs.current]);
  const loading = useMemo(() => polarisNumDS.current && polarisNumDS.current.get('status') === 'operating', [polarisNumDS.current]);

  function refresh() {

  }

  function getClusterHeader(item) {
    const checked = clusterSummaryData.checked;
    const { score, hasErrors } = clusterSummaryData[item] || {};
    const isLoading = clusterSummaryDs.status === 'loading' || loading;
    return (
      <div className={`${prefixCls}-polaris-tabs-header`}>
        <span className={`${prefixCls}-polaris-mgl-10`}>
          {formatMessage({ id: `${intlPrefix}.polaris.${item}` })}
        </span>
        <span className={`${prefixCls}-polaris-tabs-header-score`}>
          {formatMessage({ id: `${intlPrefix}.polaris.score` })}:
        </span>
        {isLoading ? <Progress type="loading" size="small" /> : <span>{checked ? `${score}%` : '-'}</span>}
        {hasErrors && <Icon type="cancel" className={`${prefixCls}-polaris-tabs-header-error`} />}
        <ProgressBar
          loading={isLoading}
          num={isLoading || !checked ? null : score}
        />
      </div>
    );
  }

  function getEnvHeader(envRecord) {
    const { envName, namespace, projectName, internal, hasErrors } = envRecord.toData() || {};
    return (
      <div className={`${prefixCls}-polaris-tabs-header`}>
        <div className={`${prefixCls}-polaris-tabs-header-item`}>
          <span className={`${prefixCls}-polaris-tabs-header-env-${internal}`}>
            {formatMessage({ id: `${intlPrefix}.polaris.internal.${internal}` })}
          </span>
        </div>
        {envName && (
          <div className={`${prefixCls}-polaris-tabs-header-item`}>
            <span className={`${prefixCls}-polaris-tabs-header-text`}>{formatMessage({ id: 'environment' })}:</span>
            <span>{envName}</span>
            {hasErrors && <Icon type="cancel" className={`${prefixCls}-polaris-tabs-header-error`} />}
          </div>
        )}
        <div className={`${prefixCls}-polaris-tabs-header-item`}>
          <span className={`${prefixCls}-polaris-tabs-header-text`}>{formatMessage({ id: 'envCode' })}:</span>
          <span>{namespace}</span>
          {!envName && hasErrors && <Icon type="cancel" className={`${prefixCls}-polaris-tabs-header-error`} />}
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

  function getClusterContent(item) {
    const checked = clusterSummaryData.checked;
    const { items: list } = clusterSummaryData[item] || {};
    if (!checked) {
      return <span className={`${prefixCls}-polaris-empty-text`}>{formatMessage({ id: `${intlPrefix}.polaris.check.null` })}</span>;
    }
    return (map(list, ({ namespace, resourceKind, resourceName, hasErrors, items }) => (
      <div key={namespace}>
        <div>
          <span>`NameSpace:${namespace}/${resourceKind}:${resourceName}`</span>
          {hasErrors && <Icon type="cancel" className={`${prefixCls}-polaris-tabs-header-error`} />}
        </div>
        {map(items, ({ message, type }) => (
          <div key={type}>{message}</div>
        ))}
      </div>
    )));
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
          <Collapse bordered={false}>
            {map(clusterSummary, (item) => (
              <Panel header={getClusterHeader(item)} key={item}>
                {getClusterContent(item)}
              </Panel>
            ))}
          </Collapse>
        </TabPane>
        <TabPane
          tab={formatMessage({ id: `${intlPrefix}.polaris.env` })}
          key="environment"
        >
          <Collapse bordered={false}>
            {map(envDetailDs.data, (envRecord) => (
              <Panel header={getEnvHeader(envRecord)} key={envRecord.id}>
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
