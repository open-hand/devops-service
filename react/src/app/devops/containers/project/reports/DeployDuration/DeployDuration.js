import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { observable, action, configure } from 'mobx';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Page, Header, Content, stores } from '@choerodon/boot';
import { Select, Button, Table, Spin } from 'choerodon-ui';
import ReactEcharts from 'echarts-for-react';
import _ from 'lodash';
import moment from 'moment';
import ChartSwitch from '../Component/ChartSwitch';
import TimePicker from '../Component/TimePicker';
import StatusTags from '../../../../components/StatusTags';
import NoChart from '../Component/NoChart';
import ContainerStore from '../../../../stores/project/container';
import './DeployDuration.scss';
import LoadingBar from '../../../../components/loadingBar/LoadingBar';
import MaxTagPopover from '../Component/MaxTagPopover';
import MouserOverWrapper from '../../../../components/MouseOverWrapper/MouserOverWrapper';

configure({ enforceActions: 'never' });

const { AppState } = stores;
const { Option } = Select;
const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

const COLOR = ['50,198,222', '116,59,231', '87,170,248', '255,177,0', '237,74,103'];
const LENGEND = [
  'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNiIgaGVpZ2h0PSIyNiIgdmlld0JveD0iMCAwIDI2IDI2Ij4KICA8Y2lyY2xlIGN4PSI0OTkiIGN5PSI2MiIgcj0iMTIiIGZpbGw9IiMzMkM2REUiIGZpbGwtb3BhY2l0eT0iLjYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgc3Ryb2tlPSIjMzJDNkRFIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtNDg2IC00OSkiLz4KPC9zdmc+Cg==',
  'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNiIgaGVpZ2h0PSIyNiIgdmlld0JveD0iMCAwIDI2IDI2Ij4KICA8Y2lyY2xlIGN4PSI0NDkiIGN5PSI1NyIgcj0iMTIiIGZpbGw9IiM3NDNCRTciIGZpbGwtb3BhY2l0eT0iLjYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgc3Ryb2tlPSIjNzQzQkU3IiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtNDM2IC00NCkiLz4KPC9zdmc+Cg==',
  'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNiIgaGVpZ2h0PSIyNiIgdmlld0JveD0iMCAwIDI2IDI2Ij4KICA8Y2lyY2xlIGN4PSI0OTkiIGN5PSI1NyIgcj0iMTIiIGZpbGw9IiM1N0FBRjgiIGZpbGwtb3BhY2l0eT0iLjYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgc3Ryb2tlPSIjNTdBQUY4IiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtNDg2IC00NCkiLz4KPC9zdmc+Cg==',
  'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB3aWR0aD0iMjYiIGhlaWdodD0iMjYiIHZpZXdCb3g9IjAgMCAyNiAyNiI+CiAgPGRlZnM+CiAgICA8Y2lyY2xlIGlkPSI1LWEiIGN4PSI0OTkiIGN5PSI1NyIgcj0iMTIiLz4KICA8L2RlZnM+CiAgPGcgZmlsbD0ibm9uZSIgZmlsbC1ydWxlPSJldmVub2RkIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtNDg2IC00NCkiPgogICAgPHVzZSBmaWxsPSIjRkZCMTAwIiBmaWxsLW9wYWNpdHk9Ii42IiB4bGluazpocmVmPSIjNS1hIiBzdHlsZT0ibWl4LWJsZW5kLW1vZGU6c2F0dXJhdGlvbiIvPgogICAgPHVzZSBzdHJva2U9IiNGRkIxMDAiIHhsaW5rOmhyZWY9IiM1LWEiLz4KICA8L2c+Cjwvc3ZnPgo=',
  'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNiIgaGVpZ2h0PSIyNiIgdmlld0JveD0iMCAwIDI2IDI2Ij4KICA8Y2lyY2xlIGN4PSI0OTkiIGN5PSI1NyIgcj0iMTIiIGZpbGw9IiNFRDRBNjciIGZpbGwtb3BhY2l0eT0iLjYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgc3Ryb2tlPSIjRUQ0QTY3IiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtNDg2IC00NCkiLz4KPC9zdmc+Cg==',
];

@observer
class DeployDuration extends Component {
  @observable env = [];

  @observable app = [];

  @observable envId = null;

  @observable appIds = [];

  @observable appArr = [];

  @observable dateArr = [];

  @observable seriesArr = [];

  @observable startTime = '';

  @observable endTime = '';

  @observable dateType = 'seven';

  handleRefresh = () => {
    this.loadEnvCards();
  };

  /**
   * 选择环境
   * @param id 环境ID
   */
  @action
  handleEnvSelect = (id) => {
    this.envId = id;
    this.loadAppByEnv(id);
    this.loadCharts();
  };

  /**
   * 选择应用
   * @param ids 应用ID
   */
  @action
  handleAppSelect = (ids) => {
    const { intl: { formatMessage } } = this.props;
    if (ids.length < 6) {
      this.appIds = ids;
      this.loadCharts();
    } else {
      this.appIds = ids.splice(0, 5);
      this.loadCharts();
      Choerodon.prompt(formatMessage({ id: 'report.deploy-duration.apps' }));
    }
  };

  componentDidMount() {
    const { ReportsStore } = this.props;
    ReportsStore.changeIsRefresh(true);
    this.loadEnvCards();
  }

  componentWillUnmount() {
    const { ReportsStore } = this.props;
    ReportsStore.setAllData([]);
    ReportsStore.setStartTime(moment().subtract(6, 'days'));
    ReportsStore.setEndTime(moment());
    ReportsStore.setPageInfo({ number: 0, totalElements: 0, size: HEIGHT <= 900 ? 10 : 15 });
    ReportsStore.setStartDate();
    ReportsStore.setEndDate();
  }

  /**
   * 获取可用环境
   */
  @action
  loadEnvCards = () => {
    const { ReportsStore } = this.props;
    const projectId = AppState.currentMenuType.id;
    ContainerStore.loadActiveEnv(projectId)
      .then((data) => {
        const env = data && data.length ? _.filter(data, ['permission', true]) : [];
        if (env.length) {
          this.env = env;
          this.envId = this.envId || env[0].id;
          this.loadAppByEnv(this.envId);
        } else {
          ReportsStore.setEchartsLoading(false);
          ReportsStore.judgeRole();
        }
        ReportsStore.changeIsRefresh(false);
      });
  };

  /**
   * 加载table数据
   */
  loadTables = () => {
    const { ReportsStore } = this.props;
    const projectId = AppState.currentMenuType.id;
    const startTime = ReportsStore.getStartTime.format().split('T')[0].replace(/-/g, '/');
    const endTime = ReportsStore.getEndTime.format().split('T')[0].replace(/-/g, '/');
    const { pageInfo } = ReportsStore;
    ReportsStore.loadDeployDurationTable(projectId, this.envId, startTime, endTime, this.appIds.slice(), pageInfo.current - 1, pageInfo.pageSize);
  };

  /**
   * 加载图表数据
   */
  @action
  loadCharts = () => {
    const { ReportsStore } = this.props;
    const projectId = AppState.currentMenuType.id;
    const startTime = ReportsStore.getStartTime.format().split('T')[0].replace(/-/g, '/');
    const endTime = ReportsStore.getEndTime.format().split('T')[0].replace(/-/g, '/');
    ReportsStore.loadDeployDurationChart(projectId, this.envId, startTime, endTime, this.appIds.slice())
      .then((res) => {
        if (res) {
          this.dateArr = res.creationDates;
          const seriesArr = [];
          const appArr = [];
          _.map(res.deployAppDTOS, (v, index) => {
            const series = {
              name: v.appName,
              symbolSize: 24,
              itemStyle: {
                color: `rgba(${COLOR[index]}, 0.6)`,
                borderColor: `rgb(${COLOR[index]})`,
              },
              data: _.map(v.deployAppDetails, c => Object.values(c)),
              type: 'scatter',
            };
            const obj = {};
            obj.name = v.appName;
            obj.icon = `image://${LENGEND[index]}`;
            seriesArr.push(series);
            appArr.push(obj);
          });
          this.seriesArr = seriesArr;
          this.appArr = appArr;
        }
      });
    this.loadTables();
  };

  /**
   * 环境ID筛选应用
   * @param envId
   */
  @action
  loadAppByEnv = (envId) => {
    const projectId = AppState.currentMenuType.id;
    ContainerStore.loadAppDataByEnv(projectId, envId)
      .then((app) => {
        this.app = app;
        if (app.length) {
          this.appIds = this.appIds.length ? this.appIds : [app[0].id];
        } else {
          this.appIds = [];
        }
        this.loadCharts();
      });
  };

  /**
   * 图表函数
   * @returns {*}
   */
  getOption() {
    const { intl: { formatMessage } } = this.props;
    return {
      legend: {
        data: this.appArr,
        borderColor: '#000',
        borderWidth: '5px',
        itemWidth: 12,
        itemHeight: 12,
      },
      toolbox: {
        feature: {
          dataZoom: {},
          brush: {
            type: [''],
          },
        },
        right: '3%',
      },
      brush: {
      },
      tooltip: {
        trigger: 'item',
        backgroundColor: '#fff',
        textStyle: {
          color: '#000',
          fontSize: 13,
          lineHeight: 20,
        },
        padding: [10, 15, 10, 15],
        extraCssText:
          'box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2); border: 1px solid #ddd; border-radius: 0;',
        formatter(params, ticket) {
          let time = params.value[1];
          if (time.split('.')[1] === '00') {
            time = `${time.toString().split('.')[0]}${formatMessage({ id: 'minutes' })}`;
          } else if (time.split('.')[0] === '0') {
            time = `${(Number(time.toString().split('.')[1]) * 0.6).toFixed()}${formatMessage({ id: 'seconds' })}`;
          } else if (time.split('.').length === 2) {
            time = `${time.toString().split('.')[0]}${formatMessage({ id: 'minutes' })}${(Number(time.toString().split('.')[1]) * 0.6).toFixed()}${formatMessage({ id: 'seconds' })}`;
          } else {
            time = null;
          }
          return `<div>
                <div>${formatMessage({ id: 'branch.issue.date' })}：${params.data[0]}</div>
                <div><span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:${params.color};"></span>${params.seriesName}：${time}</div>
              <div>`;
        },
      },
      grid: {
        left: '2%',
        right: '2%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        type: 'time',
        scale: true,
        boundaryGap: false,
        axisLine: {
          lineStyle: {
            color: '#eee',
            type: 'solid',
            width: 2,
          },
        },
        axisTick: { show: false },
        axisLabel: {
          margin: 13,
          textStyle: {
            color: 'rgba(0, 0, 0, 0.65)',
            fontSize: 12,
          },
        },
      },
      yAxis: {
        nameTextStyle: {
          fontSize: 13,
          color: '#000',
          padding: [0, 0, 0, 22],
        },
        axisTick: { show: false },
        axisLine: {
          lineStyle: {
            color: '#eee',
            type: 'solid',
            width: 2,
          },
        },
        axisLabel: {
          margin: 18,
          textStyle: {
            color: 'rgba(0, 0, 0, 0.65)',
            fontSize: 12,
          },
        },
        splitLine: {
          lineStyle: {
            show: true,
            type: 'solid',
          },
        },
        boundaryGap: false,
        name: formatMessage({ id: 'minTime' }),
        min: this.seriesArr.length ? null : 0,
        max: this.seriesArr.length ? null : 4,
        scale: true,
      },
      series: this.seriesArr,
    };
  }

  /**
   * 表格函数
   * @returns {*}
   */
  renderTable() {
    const { intl: { formatMessage }, ReportsStore } = this.props;
    const data = ReportsStore.getAllData;
    const { loading, pageInfo } = ReportsStore;

    const column = [
      {
        title: formatMessage({ id: 'app.active' }),
        key: 'status',
        render: record => (<StatusTags name={formatMessage({ id: record.status })} colorCode={record.status} error={record.error} />),
      }, {
        title: formatMessage({ id: 'report.deploy-duration.time' }),
        key: 'creationDate',
        dataIndex: 'creationDate',
      }, {
        title: formatMessage({ id: 'deploy.instance' }),
        key: 'appInstanceCode',
        dataIndex: 'appInstanceCode',
        render: text => (<MouserOverWrapper text={text} width={0.2}>{text}</MouserOverWrapper>),
      }, {
        title: formatMessage({ id: 'deploy.appName' }),
        key: 'appName',
        dataIndex: 'appName',
        render: text => (<MouserOverWrapper text={text} width={0.2}>{text}</MouserOverWrapper>),
      }, {
        title: formatMessage({ id: 'deploy.ver' }),
        key: 'appVersion',
        dataIndex: 'appVersion',
        render: text => (<MouserOverWrapper text={text} width={0.2}>{text}</MouserOverWrapper>),
      }, {
        title: formatMessage({ id: 'report.deploy-duration.user' }),
        key: 'lastUpdatedName',
        dataIndex: 'lastUpdatedName',
      },
    ];

    return (
      <Table
        rowKey={record => record.creationDate}
        dataSource={data}
        filterBar={false}
        columns={column}
        loading={loading}
        pagination={pageInfo}
        onChange={this.tableChange}
      />
    );
  }

  tableChange = (pagination) => {
    const { ReportsStore } = this.props;
    const projectId = AppState.currentMenuType.id;
    const startTime = ReportsStore.getStartTime.format().split('T')[0].replace(/-/g, '/');
    const endTime = ReportsStore.getEndTime.format().split('T')[0].replace(/-/g, '/');
    ReportsStore.loadDeployDurationTable(projectId, this.envId, startTime, endTime, this.appIds.slice(), pagination.current - 1, pagination.pageSize);
  };

  @action
  handleDateChoose = (type) => { this.dateType = type; };

  maxTagNode = (data, value) => <MaxTagPopover dataSource={data} value={value} />;

  render() {
    const { intl: { formatMessage }, history, ReportsStore } = this.props;
    const { id, name, type, organizationId } = AppState.currentMenuType;
    const echartsLoading = ReportsStore.getEchartsLoading;
    const envData = ContainerStore.getEnvCard;
    const envs = _.filter(envData, ['permission', true]);
    const isRefresh = ReportsStore.getIsRefresh;

    const envDom = this.env.length ? _.map(this.env, d => (<Option key={d.id} value={d.id}>{d.name}</Option>)) : null;

    const appDom = this.app.length ? _.map(this.app, d => (<Option key={d.id} value={d.id}>{d.name}</Option>)) : null;

    const content = (envs && envs.length ? <React.Fragment>
      <div className="c7n-report-screen c7n-report-select">
        <Select
          notFoundContent={formatMessage({ id: 'envoverview.noEnv' })}
          value={this.envId}
          label={formatMessage({ id: 'deploy.envName' })}
          className="c7n-select_200"
          onChange={this.handleEnvSelect}
          optionFilterProp="children"
          filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
          filter
        >
          {envDom}
        </Select>
        <Select
          showCheckAll={false}
          notFoundContent={formatMessage({ id: 'report.no.app.tips' })}
          value={this.appIds.length && this.appIds.slice()}
          label={formatMessage({ id: 'deploy.appName' })}
          className={`c7n-select_400 margin-more ${this.appIds.length ? 'c7n-select-multi-top' : ''}`}
          mode="multiple"
          maxTagCount={3}
          onChange={this.handleAppSelect}
          maxTagPlaceholder={this.maxTagNode.bind(this, this.app)}
          optionFilterProp="children"
          filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
          filter
        >
          {appDom}
        </Select>
        <TimePicker
          startTime={ReportsStore.getStartDate}
          endTime={ReportsStore.getEndDate}
          func={this.loadCharts}
          type={this.dateType}
          onChange={this.handleDateChoose}
          store={ReportsStore}
        />
      </div>
      <div className="c7n-report-content">
        <Spin spinning={echartsLoading}>
          <ReactEcharts
            option={this.getOption()}
            notMerge
            lazyUpdate
            style={{ height: '350px', width: '100%' }}
          />
        </Spin>
      </div>
      <div className="c7n-report-table">
        {this.renderTable()}
      </div>
    </React.Fragment> : <NoChart type="env" />);

    return (<Page
      className="c7n-region"
      service={[
        'devops-service.application.listByActive',
        'devops-service.application-instance.listDeployTime',
        'devops-service.application-instance.pageDeployTimeDetail',
        'devops-service.devops-environment.listByProjectIdAndActive',
      ]}
    >
      <Header
        title={formatMessage({ id: 'report.deploy-duration.head' })}
        backPath={`/devops/reports?type=${type}&id=${id}&name=${name}&organizationId=${organizationId}`}
      >
        <ChartSwitch
          history={history}
          current="deploy-duration"
        />
        <Button
          icon="refresh"
          onClick={this.handleRefresh}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </Header>
      <Content code="report.deploy-duration" values={{ name }}>
        {isRefresh ? <LoadingBar /> : content}
      </Content>
    </Page>);
  }
}

export default injectIntl(DeployDuration);
