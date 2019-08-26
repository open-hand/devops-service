import React, { Fragment, useMemo, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import ReactEcharts from 'echarts-for-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Table, Tooltip, Button, Icon } from 'choerodon-ui';
import { Content, Header, Page, stores } from '@choerodon/master';
import { useNodeContentStore } from './stores';
import NodePodsTable from './node-pods-table';
import Modals from './modals';

import './NodeDetail.less';


const NodeContent = observer((props) => {
  const {
    formatMessage,
    projectId,
    clusterStore,
    NodeInfoDs,
  } = useNodeContentStore();
  const {
    prefixCls,
    intlPrefix,
    permissions,
    getSelectedMenu,
  } = clusterStore;
  const { name } = getSelectedMenu;
  
  const node = NodeInfoDs.current;
  // const node = { type: 'etcdmaster', nodeName: '192.168.100.27', status: 'Ready', createTime: '2019-07-31 18:39:50', cpuTotal: '2', cpuRequest: '0.650', cpuLimit: '0.100', cpuRequestPercentage: '32.50%', cpuLimitPercentage: '5.00%', memoryTotal: '7.542Gi', memoryRequest: '3.549Gi', memoryLimit: '5.049Gi', memoryRequestPercentage: '47.05%', memoryLimitPercentage: '66.94%', podTotal: 110, podCount: 9, podPercentage: '8.18%' };

  return (
    <Fragment>
      <Modals />
      <h1>{name}</h1>
      <Content className="c7n-node-content">
        <div className="c7n-node-title">{formatMessage({ id: 'node.res' })}</div>
        <div className="c7n-node-pie">
          {node ? podPies(formatMessage, node.toData()) : null}
        </div>
        <div className="c7n-node-title">{formatMessage({ id: 'node.pods' })}</div>
        <NodePodsTable />
        {/* {this.renderTable()} */}
        {/* {showLog && <LogSiderbar visible={showLog} data={logParm} onClose={this.closeLog} />} */}
      </Content>
    </Fragment>
  );
});

export default NodeContent;


const podPies = (formatMessage, node) => {
  if (!node) { return; }
  const cpuData = {
    rv: node.cpuRequest,
    lmv: node.cpuLimit,
    resPercent: node.cpuRequestPercentage,
    limPercent: node.cpuLimitPercentage,
    total: node.cpuTotal,
  };
  const memoryPieData = {
    rv: node.memoryRequestPercentage.split('%')[0],
    lmv: node.memoryLimitPercentage.split('%')[0],
    resPercent: node.memoryRequestPercentage,
    limPercent: node.memoryLimitPercentage,
    total: 100,
  };
  const memoryData = {
    rv: node.memoryRequest,
    lmv: node.memoryLimit,
    resPercent: node.memoryRequestPercentage,
    limPercent: node.memoryLimitPercentage,
    total: node.memoryTotal,
  };
  return (<Fragment>
    <div className="c7n-node-pie-block">
      <div className="c7n-node-pie-percent">
        {cpuData.resPercent}
      </div>
      <ReactEcharts
        option={getOption(cpuData)}
        notMerge
        lazyUpdate
        style={{ height: '160px', width: '160px' }}
      />
      <div className="c7n-node-pie-inside-percent">
        {cpuData.limPercent}
      </div>
      <div className="c7n-node-pie-title">
        {formatMessage({ id: 'cluster.cpu' })}
      </div>
      {pieDes(formatMessage, cpuData)}
    </div>
    <div className="c7n-node-pie-block">
      <div className="c7n-node-pie-percent">
        {memoryData.resPercent}
      </div>
      <ReactEcharts
        option={getOption(memoryPieData)}
        notMerge
        lazyUpdate
        style={{ height: '160px', width: '160px' }}
      />
      <div className="c7n-node-pie-inside-percent">
        {memoryData.limPercent}
      </div>
      <div className="c7n-node-pie-title">
        {formatMessage({ id: 'cluster.memory' })}
      </div>
      {pieDes(formatMessage, memoryData)}
    </div>
    <div className="c7n-node-pie-block">
      <div className="c7n-node-pie-percent" />
      <ReactEcharts
        option={getPodOption(node)}
        notMerge
        lazyUpdate
        style={{ height: '160px', width: '160px' }}
      />
      <div className="c7n-node-pie-pod-percent">
        {node.podPercentage}
      </div>
      <div className="c7n-node-pie-title">
        {formatMessage({ id: 'node.pod.allocated' })}
      </div>
      <div className="c7n-node-pie-info">
        <span className="c7n-node-pie-info-span rv" />
        <span>{formatMessage({ id: 'node.allocated' })}</span>
        <span>{node.podCount}</span>
      </div>
      <div className="c7n-node-pie-info">
        <span className="c7n-node-pie-info-span" />
        <span>{formatMessage({ id: 'node.allV' })}</span>
        <span>{node.podTotal}</span>
      </div>
    </div>
  </Fragment>);
};

const getOption = (data) => {
  const lmvSeries = data.total - data.lmv > 0 ? {
    type: 'pie',
    radius: ['40%', '68%'],
    hoverAnimation: false,
    label: { show: false },
    data: [
      { value: data.lmv,
        name: 'limitValue',
        itemStyle: { color: '#57AAF8' },
      },
      { value: data.total - data.lmv, name: 'value', itemStyle: { color: 'rgba(0,0,0,0.08)' } },
    ],
  } : {
    type: 'pie',
    radius: ['40%', '68%'],
    hoverAnimation: false,
    label: { show: false },
    data: [
      { value: data.lmv,
        name: 'limitValue',
        itemStyle: { color: '#57AAF8' },
      },
    ],
  };
  const rvSeries = data.total - data.rv > 0 ? {
    hoverAnimation: false,
    type: 'pie',
    radius: ['75%', '95%'],
    label: { show: false },
    data: [
      {
        value: data.rv,
        name: 'requestValue',
        itemStyle: { color: '#00BFA5' },
      },
      { value: data.total - data.rv, name: 'value', itemStyle: { color: 'rgba(0,0,0,0.08)' } },
    ],
  } : {
    type: 'pie',
    radius: ['75%', '95%'],
    hoverAnimation: false,
    label: { show: false },
    data: [
      { value: data.rv,
        name: 'limitValue',
        itemStyle: { color: '#00BFA5' },
      },
    ],
  };
  return {
    tooltip: {
      show: false,
    },
    legend: {
      show: false,
    },
    series: [
      lmvSeries,
      rvSeries,
    ],
  };
};


const pieDes = (formatMessage, data) => (<Fragment>
  <div className="c7n-node-pie-info">
    <span className="c7n-node-pie-info-span rv" />
    <span>{formatMessage({ id: 'node.rv' })}</span>
    <span>{data.rv}</span>
  </div>
  <div className="c7n-node-pie-info">
    <span className="c7n-node-pie-info-span lmv" />
    <span>{formatMessage({ id: 'node.lmv' })}</span>
    <span>{data.lmv}</span>
  </div>
  <div className="c7n-node-pie-info">
    <span className="c7n-node-pie-info-span" />
    <span>{formatMessage({ id: 'node.allV' })}</span>
    <span>{data.total}</span>
  </div>
</Fragment>);


const getPodOption = (node) => ({
  tooltip: {
    show: false,
  },
  legend: {
    show: false,
  },
  series: [
    {
      hoverAnimation: false,
      type: 'pie',
      radius: ['75%', '95%'],
      label: { show: false },
      data: [
        {
          value: node.podCount,
          name: 'requestValue',
          itemStyle: { color: '#00BFA5' },
        },
        { value: node.podTotal, name: 'value', itemStyle: { color: 'rgba(0,0,0,0.08)' } },
      ],
    },
  ],
});
