import React, { Component, Fragment } from 'react/index';
import { observer } from 'mobx-react';
import { withRouter } from "react-router-dom";
import { injectIntl, FormattedMessage } from 'react-intl';
import { Table, Tooltip, Button, Icon } from 'choerodon-ui';
import ReactEcharts from 'echarts-for-react';
import { Content, Header, Page, stores } from '@choerodon/boot';
import StatusTags from '../../../components/StatusTags';
import TimePopover from '../../../components/timePopover';
import MouserOverWrapper from '../../../components/MouseOverWrapper';
import '../../main.scss';
import LogSiderbar from '../logSiderbar';
import './index.scss';

const { AppState } = stores;

@observer
class NodeDetail extends Component {
  constructor(props) {
    super(...arguments);
    this.state = {
      showLog: false,
    };
    this.clusterId = this.props.match.params.clusterId;
    this.nodeName = this.props.history.location.search.split('&node=')[1];
  }

  componentDidMount() {
    this.loadPodTable();
    this.loadNodePie();
  }

  loadPodTable = () => {
    const { ClusterStore } = this.props;
    const { organizationId } = AppState.currentMenuType;
    ClusterStore.loadPodTable(organizationId, this.clusterId, this.nodeName);
  };

  loadNodePie = () => {
    const { ClusterStore } = this.props;
    const { organizationId } = AppState.currentMenuType;
    ClusterStore.loadNodePie(organizationId, this.clusterId, this.nodeName);
  };

  getOption = (data) => {
    const lmvSeries =  data.total - data.lmv  > 0 ? {
      type: 'pie',
      radius: ['40%', '68%'],
      hoverAnimation: false,
      label: { show: false },
      data: [
        { value: data.lmv,
          name: 'limitValue',
          itemStyle: { color: '#57AAF8' },
        },
        { value: data.total - data.lmv , name: 'value', itemStyle: { color: 'rgba(0,0,0,0.08)' }},
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
    const rvSeries =  data.total - data.rv  > 0 ? {
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
        { value: data.total - data.rv, name: 'value', itemStyle: { color: 'rgba(0,0,0,0.08)' }},
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

  getPodOption = () => {
    const { ClusterStore } = this.props;
    const node = ClusterStore.getNode;

    return {
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
            { value: node.podTotal, name: 'value', itemStyle: { color: 'rgba(0,0,0,0.08)' }},
          ],
        },
      ],
    };
  };


  /**
   * 显示日志
   * @param record 容器record
   */
  showLog = record => {
    const logData = {
      namespace: record.namespace,
      podName: record.name,
      clusterId: this.clusterId,
      containerName: record.containersForLogs[0].containerName,
      logId: record.containersForLogs[0].logId,
    };
    this.setState({
      logParm: logData,
      showLog: true,
    });
  };

  closeLog = () => {
    this.setState({ showLog: false });
  };

  /**
   * 表格函数
   * @returns {*}
   */
  renderTable() {
    const { intl: { formatMessage }, ClusterStore } = this.props;
    const data = ClusterStore.getPodData;
    const { loading, pageInfo } = ClusterStore;
    const { paras } = ClusterStore.getInfo;
    const column = [
      {
        title: formatMessage({ id: 'status' }),
        key: 'status',
        render: record => (<StatusTags
          name={record.status}
          colorCode={record.status}
          style={{ minWidth: 68 }}
        />),
      }, {
        title: formatMessage({ id: 'node.podName' }),
        key: 'name',
        dataIndex: 'name',
        render: name => (<MouserOverWrapper text={name} width={0.2}>{name}</MouserOverWrapper>),
      },  {
        title: formatMessage({ id: 'node.rTimes' }),
        key: 'restartCount',
        dataIndex: 'restartCount',
      }, {
        key: 'creationDate',
        title: formatMessage({ id: 'ciPipeline.createdAt' }),
        dataIndex: 'creationDate',
        render: creationDate => <TimePopover content={creationDate} />,
      }, {
        align: 'right',
        key: 'action',
        render: record => (
          <Tooltip title={<FormattedMessage id="node.log" />}>
            <Button
              size="small"
              shape="circle"
              onClick={this.showLog.bind(this, record)}
            >
              <Icon type="find_in_page" />
            </Button>
          </Tooltip>
        ),
      },
    ];

    return (
      <Table
        rowKey={record => record.id}
        dataSource={data.slice()}
        columns={column}
        loading={loading}
        pagination={pageInfo}
        onChange={this.tableChange}
        filters={paras.slice()}
        filterBarPlaceholder={formatMessage({ id: "filter" })}
      />
    );
  }


  /**
   * table 操作
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  tableChange = (pagination, filters, sorter, paras) => {
    const { ClusterStore } = this.props;
    const { organizationId } = AppState.currentMenuType;
    ClusterStore.setInfo({ filters, sort: sorter, paras });
    const sort = { field: "", order: "desc" };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === "ascend") {
        sort.order = "asc";
      } else if (sorter.order === "descend") {
        sort.order = "desc";
      }
    }
    let searchParam = {};
    const page = pagination.current;
    if (Object.keys(filters).length) {
      searchParam = filters;
    }
    const postData = {
      searchParam,
      param: paras.toString(),
    };
    ClusterStore.loadPodTable(
      organizationId,
      this.clusterId,
      this.nodeName,
      page,
      pagination.pageSize,
      sort,
      postData
    );
  };

  pieDes = (data) => {
    const { intl: { formatMessage } } = this.props;

    return (<Fragment>
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
  };

  podPies = () => {
    const { ClusterStore, intl: { formatMessage } } = this.props;
    const node = ClusterStore.getNode;
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
          option={this.getOption(cpuData)}
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
        {this.pieDes(cpuData)}
      </div>
      <div className="c7n-node-pie-block">
        <div className="c7n-node-pie-percent">
          {memoryData.resPercent}
        </div>
        <ReactEcharts
          option={this.getOption(memoryPieData)}
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
        {this.pieDes(memoryData)}
      </div>
      <div className="c7n-node-pie-block">
        <div className="c7n-node-pie-percent" />
        <ReactEcharts
          option={this.getPodOption()}
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
    </Fragment>)
  };

  render() {
    const { type, organizationId, name } = AppState.currentMenuType;
    const { intl: { formatMessage } } = this.props;
    const { showLog, logParm } = this.state;

    return (
      <Page>
        <Header title={<FormattedMessage id="node.head" />} backPath={`/devops/cluster?type=${type}&id=${organizationId}&name=${name}&organizationId=${organizationId}`} />
        <Content code="node" values={{ name: this.nodeName }}>
          <div className="c7n-node-title">{formatMessage({ id: 'node.res' })}</div>
          <div className="c7n-node-pie">
            {this.podPies()}
          </div>
          <div className="c7n-node-title">{formatMessage({ id: 'node.pods' })}</div>
          {this.renderTable()}
          {showLog && <LogSiderbar visible={showLog} data={logParm} onClose={this.closeLog}/>}
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(NodeDetail));
