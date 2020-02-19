import React, { useState, Fragment, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Tabs } from 'choerodon-ui/pro';
import map from 'lodash/map';
import isEmpty from 'lodash/isEmpty';
import { Collapse, Progress, Icon } from 'choerodon-ui';
import { useClusterMainStore } from '../../../../stores';
import { useClusterContentStore } from '../../stores';
import ProgressBar from '../components/Progress';

import './index.less';

const { TabPane } = Tabs;
const { Panel } = Collapse;

const collapseDetail = observer(({ loading }) => {
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
  
  const clusterSummary = useMemo(() => (['healthCheck', 'imageCheck', 'networkCheck', 'resourceCheck', 'securityCheck']), []);
  const clusterSummaryData = useMemo(() => {
    if (clusterSummaryDs.current) {
      return clusterSummaryDs.current.toData();
    }
    return {};
  }, [clusterSummaryDs.current]);
  const isLoading = useMemo(() => {
    const flag = polarisNumDS.current && polarisNumDS.current.get('status') === 'operating';
    return loading || flag || clusterSummaryDs.status === 'loading';
  }, [loading, clusterSummaryDs.status, polarisNumDS.current]);

  function refresh() {

  }

  function getClusterHeader(item) {
    const checked = clusterSummaryData.checked;
    const { score, hasErrors } = clusterSummaryData[item] || {};
    return (
      <div className={`${prefixCls}-polaris-tabs-header`}>
        <span className={`${prefixCls}-polaris-mgl-10`}>
          {formatMessage({ id: `${intlPrefix}.polaris.${item}` })}
        </span>
        <span className={`${prefixCls}-polaris-tabs-header-score`}>
          {formatMessage({ id: `${intlPrefix}.polaris.score` })}:
        </span>
        {isLoading ? <Progress type="loading" size="small" /> : <span>{checked ? `${score}%` : '-'}</span>}
        {!isLoading && hasErrors && <Icon type="cancel" className={`${prefixCls}-polaris-tabs-header-error`} />}
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
            {!isLoading && hasErrors && <Icon type="cancel" className={`${prefixCls}-polaris-tabs-header-error`} />}
          </div>
        )}
        <div className={`${prefixCls}-polaris-tabs-header-item`}>
          <span className={`${prefixCls}-polaris-tabs-header-text`}>{formatMessage({ id: 'envCode' })}:</span>
          <span>{namespace}</span>
          {!isLoading && !envName && hasErrors && <Icon type="cancel" className={`${prefixCls}-polaris-tabs-header-error`} />}
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
      return <span className={`${prefixCls}-polaris-tabs-content`}>{formatMessage({ id: `${intlPrefix}.polaris.check.null` })}</span>;
    }
    if (isLoading) {
      return <span className={`${prefixCls}-polaris-tabs-content`}>{formatMessage({ id: `${intlPrefix}.polaris.check.operating` })}</span>;
    }
    if (isEmpty(list)) {
      return (
        <div className={`${prefixCls}-polaris-tabs-content`}>
          <Icon type="done" className={`${prefixCls}-polaris-tabs-content-icon-success`} />
          <span>{formatMessage({ id: `${intlPrefix}.polaris.check.success` })}</span>
        </div>
      );
    }
    return (map(list, ({ namespace, resourceKind, resourceName, hasErrors, items }, index) => (
      <div key={`${namespace}-${index}`} className={`${prefixCls}-polaris-tabs-content`}>
        <div className={`${prefixCls}-polaris-tabs-content-title`}>
          <span>{`NameSpace: ${namespace} / ${resourceKind}: ${resourceName}`}</span>
          {hasErrors && <Icon type="cancel" className={`${prefixCls}-polaris-tabs-header-error`} />}
        </div>
        {map(items, ({ message, type, severity }) => (
          <div key={type} className={`${prefixCls}-polaris-tabs-content-des`}>
            <Icon type={severity === 'warning' ? 'priority_high' : 'close'} className={`${prefixCls}-polaris-tabs-content-icon-${severity}`} />
            <span>{message}</span>
          </div>
        ))}
      </div>
    )));
  }

  function getEnvContent(envRecord) {
    const checked = envRecord.get('checked');
    const items = envRecord.get('items');
    if (!checked) {
      return <span className={`${prefixCls}-polaris-tabs-content`}>{formatMessage({ id: `${intlPrefix}.polaris.check.null` })}</span>;
    }
    if (isLoading) {
      return <span className={`${prefixCls}-polaris-tabs-content`}>{formatMessage({ id: `${intlPrefix}.polaris.check.operating` })}</span>;
    }
    return (map(items, ({ hasErrors, detailJson }, index) => {
      const data = JSON.parse(detailJson);
      const { kind, name, podResult } = data || {};
      const containers = podResult ? podResult.containerResults : [];
      const results = podResult ? podResult.results : {};
      return (
        <div key={`${name}-${index}`} className={`${prefixCls}-polaris-tabs-content`}>
          <div className={`${prefixCls}-polaris-tabs-content-title-weight`}>
            <span>{`${kind}: ${name}`}</span>
            {hasErrors && <Icon type="cancel" className={`${prefixCls}-polaris-tabs-header-error`} />}
          </div>
          <div className={`${prefixCls}-polaris-tabs-content-title`}>
            <span>Pod Spec:</span>
          </div>
          {isEmpty(results) ? (
            <div className={`${prefixCls}-polaris-tabs-content-env`}>
              <Icon type="done" className={`${prefixCls}-polaris-tabs-content-icon-success`} />
              <span>{formatMessage({ id: `${intlPrefix}.polaris.check.success` })}</span>
            </div>
          ) : (map(results, ({ message, type, severity }) => (
            <div key={type} className={`${prefixCls}-polaris-tabs-content-des`}>
              <Icon type={severity === 'warning' ? 'priority_high' : 'close'} className={`${prefixCls}-polaris-tabs-content-icon-${severity}`} />
              <span>{message}</span>
            </div>
          )))}
          {map(containers, ({ name: containerName, results: containerResults }) => (<Fragment>
            <div className={`${prefixCls}-polaris-tabs-content-title`} key={containerName}>
              <span>{`Container: ${containerName}`}</span>
            </div>
            {isEmpty(containerResults) ? (
              <div className={`${prefixCls}-polaris-tabs-content-env`}>
                <Icon type="done" className={`${prefixCls}-polaris-tabs-content-icon-success`} />
                <span>{formatMessage({ id: `${intlPrefix}.polaris.check.success` })}</span>
              </div>
            ) : (map(containerResults, ({ message, type, severity }) => (
              <div key={type} className={`${prefixCls}-polaris-tabs-content-des`}>
                <Icon type={severity === 'warning' ? 'priority_high' : 'close'} className={`${prefixCls}-polaris-tabs-content-icon-${severity}`} />
                <span>{message}</span>
              </div>
            )))}
          </Fragment>))}
        </div>
      );
    }));
  }

  return (
    <div className={`${prefixCls}-polaris-wrap-collapse`}>
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
                {getEnvContent(envRecord)}
              </Panel>
            ))}
          </Collapse>
        </TabPane>
      </Tabs>
    </div>
  );
});

export default collapseDetail;
