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
import StatusTags from '../../../../components/StatusTags';
import TimePicker from '../Component/TimePicker';
import NoChart from '../Component/NoChart';
import ContainerStore from '../../../../stores/project/container';
import '../DeployDuration/DeployDuration.scss';
import { getAxis } from '../../../../utils';
import LoadingBar from '../../../../components/loadingBar/LoadingBar';
import MaxTagPopover from '../Component/MaxTagPopover';
import MouserOverWrapper from '../../../../components/MouseOverWrapper/MouserOverWrapper';

configure({ enforceActions: 'never' });

const { AppState } = stores;
const { Option } = Select;
const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

@observer
class DeployTimes extends Component {
  @observable env = [];

  @observable app = [];

  @observable envIds = [];

  @observable appId = 'all';

  @observable appArr = [];

  @observable dateArr = [];

  @observable successArr = [];

  @observable failArr = [];

  @observable allArr = [];

  @observable dateType = 'seven';

  handleRefresh = () => {
    this.loadEnvCards();
    this.loadApps();
  };

  /**
   * 选择环境
   * @param ids 环境ID
   */
  @action
  handleEnvSelect = (ids) => {
    this.envIds = ids;
    this.loadCharts();
  };

  /**
   * 选择应用
   * @param id 应用ID
   */
  @action
  handleAppSelect = (id) => {
    this.appId = id;
    this.loadCharts();
  };

  componentDidMount() {
    const { ReportsStore } = this.props;
    ReportsStore.changeIsRefresh(true);
    this.loadEnvCards();
    this.loadApps();
  }

  componentWillUnmount() {
    const { ReportsStore } = this.props;
    ReportsStore.setAllData([]);
    ReportsStore.setStartTime(moment().subtract(6, 'days'));
    ReportsStore.setEndTime(moment());
    ReportsStore.setStartDate();
    ReportsStore.setEndDate();
    ReportsStore.setPageInfo({ number: 0, totalElements: 0, size: HEIGHT <= 900 ? 10 : 15 });
  }

  /**
   * 获取可用环境
   */
  @action
  loadEnvCards = () => {
    const { history: { location: { state } } } = this.props;
    const projectId = AppState.currentMenuType.id;
    let historyEnvsId = null;
    if (state && state.envIds) {
      historyEnvsId = state.envIds;
    }
    ContainerStore.loadActiveEnv(projectId)
      .then((data) => {
        const env = data && data.length ? _.filter(data, ['permission', true]) : [];
        if (env.length) {
          let selectEnv = this.envIds.length ? this.envIds : [env[0].id];
          if (historyEnvsId) {
            selectEnv = historyEnvsId;
          }
          this.env = env;
          this.envIds = selectEnv;
        }
        this.loadCharts();
      });
  };

  /**
   * 加载应用
   */
  @action
  loadApps = () => {
    const {
      ReportsStore,
      history: { location: { state } },
    } = this.props;
    const { id } = AppState.currentMenuType;
    let historyAppId = null;
    if (state && state.appId) {
      historyAppId = state.appId;
    }
    ReportsStore.loadApps(id)
      .then((app) => {
        if (app.length) {
          let selectApp = this.appId || 'all';
          if (historyAppId) {
            selectApp = historyAppId;
          }
          this.app = app;
          this.appId = selectApp;
        } else {
          ReportsStore.judgeRole();
        }
      });
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
    const appID = (this.appId === 'all') ? [] : this.appId;
    ReportsStore.loadDeployTimesChart(projectId, appID, startTime, endTime, this.envIds.slice())
      .then((res) => {
        if (res) {
          this.dateArr = res.creationDates;
          this.successArr = res.deploySuccessFrequency;
          this.failArr = res.deployFailFrequency;
          this.allArr = res.deployFrequencys;
        }
      });
    this.loadTables();
  };

  /**
   * 加载table数据
   */
  @action
  loadTables = () => {
    const { ReportsStore } = this.props;
    const projectId = AppState.currentMenuType.id;
    const startTime = ReportsStore.getStartTime.format().split('T')[0].replace(/-/g, '/');
    const endTime = ReportsStore.getEndTime.format().split('T')[0].replace(/-/g, '/');
    const appID = (this.appId === 'all') ? [] : this.appId;
    const { pageInfo } = ReportsStore;
    ReportsStore.loadDeployTimesTable(projectId, appID, startTime, endTime, this.envIds.slice(), pageInfo.current - 1, pageInfo.pageSize);
  };

  /**
   * 渲染图表
   * @returns {*}
   */
  getOption() {
    const { intl: { formatMessage }, ReportsStore } = this.props;
    const val = [{ name: `${formatMessage({ id: 'report.build-number.fail' })}` }, { name: `${formatMessage({ id: 'report.build-number.success' })}` }, { name: `${formatMessage({ id: 'report.build-number.total' })}` }];
    val[0].value = _.reduce(this.failArr, (sum, n) => sum + n, 0);
    val[1].value = _.reduce(this.successArr, (sum, n) => sum + n, 0);
    val[2].value = _.reduce(this.allArr, (sum, n) => sum + n, 0);
    const startTime = ReportsStore.getStartTime;
    const endTime = ReportsStore.getEndTime;
    const successArr = this.successArr ? this.successArr.slice() : [];
    const failArr = this.failArr ? this.failArr.slice() : [];
    const allArr = this.allArr ? this.allArr.slice() : [];
    const { xAxis, yAxis } = getAxis(startTime, endTime, this.dateArr ? this.dateArr.slice() : [], { successArr, failArr, allArr });
    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'none',
        },
        backgroundColor: '#fff',
        textStyle: {
          color: '#000',
          fontSize: 13,
          lineHeight: 20,
        },
        padding: [10, 15, 10, 15],
        extraCssText:
          'box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2); border: 1px solid #ddd; border-radius: 0;',
        formatter(params) {
          if (params[1].value || params[0].value) {
            const total = params[0].value + params[1].value;
            return `<div>
                <div>${formatMessage({ id: 'branch.issue.date' })}：${params[1].name}</div>
                <div><span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:${params[1].color};"></span>${formatMessage({ id: 'appstore.deploy' })}${params[1].seriesName}：${params[1].value}</div>
                <div><span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:${params[0].color};"></span>${formatMessage({ id: 'appstore.deploy' })}${params[0].seriesName}：${params[0].value}</div>
                <div>${formatMessage({ id: 'appstore.deploy' })}${formatMessage({ id: 'report.build-number.total' })}：${total}</div>
                <div>${formatMessage({ id: 'appstore.deploy' })}${formatMessage({ id: 'report.build-number.success.rate' })}：${((params[0].value / total) * 100).toFixed(2)}%</div>
              <div>`;
          }
        },
      },
      legend: {
        left: 'right',
        itemWidth: 14,
        itemGap: 20,
        formatter(name) {
          let count = 0;
          _.map(val, (data) => {
            if (data.name === name) {
              count = data.value;
            }
          });
          return `${name}：${count}`;
        },
        selectedMode: false,
      },
      grid: {
        left: '2%',
        right: '3%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        axisTick: { show: false },
        axisLine: {
          lineStyle: {
            color: '#eee',
            type: 'solid',
            width: 2,
          },
        },
        splitLine: {
          lineStyle: {
            color: ['#eee'],
            width: 1,
            type: 'solid',
          },
        },
        data: xAxis,
        axisLabel: {
          margin: 13,
          textStyle: {
            color: 'rgba(0, 0, 0, 0.65)',
            fontSize: 12,
          },
          formatter(value) {
            return value.slice(5, 10).replace('-', '/');
          },
        },
      },
      yAxis: {
        name: `${formatMessage({ id: 'report.build-number.yAxis' })}`,
        min: yAxis.allArr.length ? null : 0,
        max: yAxis.allArr.length ? null : 4,
        type: 'value',
        nameTextStyle: {
          fontSize: 13,
          color: '#000',
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
            color: '#eee',
            type: 'solid',
            width: 1,
          },
        },
      },
      series: [
        {
          name: `${formatMessage({ id: 'report.build-number.success' })}`,
          type: 'bar',
          itemStyle: {
            color: '#00BFA5',
            emphasis: {
              shadowBlur: 10,
              shadowColor: 'rgba(0,0,0,0.20)',
            },
          },
          barWidth: '40%',
          stack: 'total',
          data: yAxis.successArr,
        },
        {
          name: `${formatMessage({ id: 'report.build-number.fail' })}`,
          type: 'bar',
          itemStyle: {
            color: '#F44336',
            emphasis: {
              shadowBlur: 10,
              shadowColor: 'rgba(0,0,0,0.20)',
            },
          },
          barWidth: '40%',
          stack: 'total',
          data: yAxis.failArr,
        },
        {
          name: `${formatMessage({ id: 'report.build-number.total' })}`,
          type: 'bar',
          color: 'transparent',
          stack: 'total',
        },
      ],
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
    const appID = (this.appId === 'all') ? [] : this.appId;
    ReportsStore.loadDeployTimesTable(projectId, appID, startTime, endTime, this.envIds.slice(), pagination.current - 1, pagination.pageSize);
  };

  @action
  handleDateChoose = (type) => { this.dateType = type; };

  maxTagNode = (data, value) => <MaxTagPopover dataSource={data} value={value} />;

  render() {
    const { intl: { formatMessage }, history, location: { search }, ReportsStore } = this.props;
    const { id, name, type, organizationId } = AppState.currentMenuType;
    const echartsLoading = ReportsStore.getEchartsLoading;
    const isRefresh = ReportsStore.getIsRefresh;
    const backPath = search.includes("deploy-overview")
      ? "deploy-overview"
      : "reports";

    const envDom = this.env.length ? _.map(this.env, d => (<Option key={d.id} value={d.id}>{d.name}</Option>)) : null;

    const appDom = this.app.length ? _.map(this.app, d => (<Option key={d.id} value={d.id}>{d.name}</Option>)) : null;

    const content = (this.app.length ? <React.Fragment>
      <div className="c7n-report-screen c7n-report-select">
        <Select
          notFoundContent={formatMessage({ id: 'envoverview.noEnv' })}
          value={this.envIds.length && this.envIds.slice()}
          label={formatMessage({ id: 'deploy.envName' })}
          className={`c7n-select_400 ${this.envIds.length ? 'c7n-select-multi-top' : ''}`}
          mode="multiple"
          maxTagCount={3}
          onChange={this.handleEnvSelect}
          maxTagPlaceholder={this.maxTagNode.bind(this, this.env)}
          optionFilterProp="children"
          filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
          filter
        >
          {envDom}
        </Select>
        <Select
          notFoundContent={formatMessage({ id: 'envoverview.unlist' })}
          value={appDom ? this.appId : null}
          className="c7n-select_200 margin-more"
          label={formatMessage({ id: 'deploy.appName' })}
          onChange={this.handleAppSelect}
          optionFilterProp="children"
          filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
          filter
        >
          {appDom}
          {appDom ? <Option key="all" value="all">{formatMessage({ id: 'report.all-app' })}</Option> : null}
        </Select>
        <TimePicker
          startTime={ReportsStore.getStartDate}
          endTime={ReportsStore.getEndDate}
          type={this.dateType}
          onChange={this.handleDateChoose}
          func={this.loadCharts}
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
    </React.Fragment> : <NoChart type="app" />);

    return (<Page
      className="c7n-region"
      service={[
        'devops-service.application.listByActive',
        'devops-service.application-instance.listDeployFrequency',
        'devops-service.application-instance.pageDeployFrequencyDetail',
      ]}
    >
      <Header
        title={formatMessage({ id: 'report.deploy-times.head' })}
        backPath={`/devops/${backPath}?type=${type}&id=${id}&name=${name}&organizationId=${organizationId}`}
      >
        <ChartSwitch
          history={history}
          current="deploy-times"
        />
        <Button
          icon="refresh"
          onClick={this.handleRefresh}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </Header>
      <Content code="report.deploy-times" values={{ name }}>
        {isRefresh ? <LoadingBar /> : content}
      </Content>
    </Page>);
  }
}

export default injectIntl(DeployTimes);
