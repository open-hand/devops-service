import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Page, Header, Content } from '@choerodon/boot';
import { Select, Button, Tooltip, Spin } from 'choerodon-ui';
import ReactEcharts from 'echarts-for-react';
import _ from 'lodash';
import moment from 'moment';
import ChartSwitch from '../Component/ChartSwitch';
import TimePicker from '../Component/TimePicker';
import NoChart from '../Component/NoChart';
import LoadingBar from '../../../../components/loadingBar/LoadingBar';
import { HEIGHT} from "../../../../common/Constants";

import "./CodeQuality.scss";

const { Option } = Select;
const OBJECT_TYPE = {
  issue: [
    { name: "bugs", color: "#5266d4" },
    { name: "codeSmells", color: "#2196f3" },
    { name: "vulnerabilities", color: "#00bcd4" },
  ],
  coverage: [
    { name: "linesToCover", color: "#2196f3" },
    { name: "coverLines", color: "#00bcd4" },
  ],
  duplicate: [
    { name: "nclocs", color: "#2196f3" },
    { name: "duplicatedLines", color: "#00bcd4" },
  ],
};

@injectIntl
@inject('AppState')
@observer
class CodeQuality extends Component {
  constructor(props) {
    super(props);
    this.state = {
      dateType: 'seven',
      objectType: "issue",
    };
  }

  componentDidMount() {
    const {
      ReportsStore,
    } = this.props;
    ReportsStore.changeIsRefresh(true);
    this.loadDatas();
  }

  componentWillUnmount() {
    const { ReportsStore } = this.props;
    ReportsStore.setAllData([]);
    ReportsStore.setCodeQuality({});
    ReportsStore.setStartTime(moment().subtract(6, 'days'));
    ReportsStore.setEndTime(moment());
    ReportsStore.setAppId(null);
    ReportsStore.setPageInfo({ number: 0, totalElements: 0, size: HEIGHT <= 900 ? 10 : 15 });
    ReportsStore.setStartDate();
    ReportsStore.setEndDate();
    ReportsStore.setAllApps([]);
  }

  /**
   * 加载数据
   */
  loadDatas = () => {
    const {
      ReportsStore,
      AppState: { currentMenuType: { projectId } },
      location: { state },
    } = this.props;
    const { appId, type } = state || {};
    ReportsStore.loadAllApps(projectId)
      .then((data) => {
        const appData = data && data.length ? _.filter(data, ['permission', true]) : [];
        if (appData.length) {
          const selectApp = appId || appData[0].id;
          ReportsStore.setAppId(selectApp);
          this.loadCharts();
        }
      });
    type && this.setState({ objectType: type });
  };

  /**
   * 加载图表
   */
  loadCharts = () => {
    const {
      ReportsStore,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    const { objectType } = this.state;
    const { getStartTime, getEndTime, getAppId } = ReportsStore;
    const startTime = getStartTime.format().split('T')[0].replace(/-/g, '/');
    const endTime = getEndTime.format().split('T')[0].replace(/-/g, '/');
    ReportsStore.loadCodeQuality(projectId, getAppId, objectType, startTime, endTime);
  };

  /**
   * 刷新
   */
  handleRefresh = () => {
    const {
      ReportsStore,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    ReportsStore.loadAllApps(projectId);
    this.loadCharts();
  };

  /**
   * 选择应用
   * @param value
   */
  handleAppSelect = (value) => {
    const { ReportsStore } = this.props;
    ReportsStore.setAppId(value);
    this.loadCharts();
  };

  /**
   * 选择对象类型
   * @param value
   */
  handleTypeSelect = (value) => {
    this.setState({ objectType: value }, () => this.loadCharts());
  };

  /**
   * 选择时间
   * @param type
   */
  handleDateChoose = (type) => {
    this.setState({ dateType: type });
  };

  /**
   * 图表函数
   */
  getOption() {
    const {
      intl: { formatMessage },
      ReportsStore,
    } = this.props;
    const { objectType } = this.state;
    const { getCodeQuality } = ReportsStore;
    const series = [];
    const legend = [];
    const dates = getCodeQuality.dates || [];
    _.map(OBJECT_TYPE[objectType], ({ name, color }) => {
      if (getCodeQuality[name]) {
        series.push(
          {
            name: formatMessage({id: `report.code-quality.${name}`}),
            type: "line",
            symbol: "circle",
            showSymbol: false,
            itemStyle: {
              color: color,
            },
            data: _.map(dates, (item, index) => [item, getCodeQuality[name][index]]),
          }
        );
        legend.push(
          {
            name: formatMessage({id: `report.code-quality.${name}`}),
            icon: "line",
          }
        )
      }
    });

    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'line',
        },
        backgroundColor: '#fff',
        textStyle: {
          color: '#000',
          fontSize: 13,
          lineHeight: 20,
        },
        padding: [10, 15],
        extraCssText:
          'box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2); border: 1px solid #ddd; border-radius: 0;',
        formatter(params) {
          const percent = (params[1].value[1] / params[0].value[1] * 100).toFixed(1);
          const list = _.map(params, ({ color, value, seriesName }) => (
            `<div>
              <span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:${color};"></span>
              <span>${seriesName}：${value[1]}</span>
            </div>`
          ));
          return `<div>
            <div><span>${formatMessage({ id: "report.date" })}：${params[0].value[0].split('+')[0].replace(/T/g, ' ')}</span></div>
            ${objectType !== "issue" ?
            `<div><span>${formatMessage({ id: `report.code-quality.type.${objectType}`})}：${percent}%</span></div>` : ""
            }
            ${list.join("")}
          </div>`
        },
      },
      legend: {
        data: legend,
        left: "right",
        itemGap: 40,
        itemWidth: 34,
        selectedMode: false,
        padding: [5, 10, 5, 0],
      },
      grid: {
        left: 'left',
        right: 10,
        bottom: '3%',
        containLabel: true,
      },
      dataZoom: [
        {
          startValue: dates[0],
        },
        {
          type: 'inside',
        },
      ],
      xAxis: {
        type: 'time',
        axisTick: { show: false },
        axisLine: {
          lineStyle: {
            color: '#eee',
            type: 'solid',
            width: 2,
          },
        },
        axisLabel: {
          margin: 13,
          textStyle: {
            color: 'rgba(0, 0, 0, 0.65)',
            fontSize: 12,
          },
          align: 'right',
        },
        splitLine: {
          lineStyle: {
            color: ['#eee'],
            width: 1,
            type: 'solid',
          },
        },
      },
      yAxis: {
        name: formatMessage({ id: objectType === "issue" ? "report.code-quality.number" : "report.code-quality.rows" }),
        type: 'value',
        nameTextStyle: {
          fontSize: 13,
          color: '#000',
          padding: dates && dates.length ? null : [0, 0, 0, 25],
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
          margin: 13,
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
        scale: true,
      },
      series: series,
    };
  }

  render() {
    const {
      intl: { formatMessage },
      history,
      ReportsStore,
      AppState: {
        currentMenuType: { name },
      },
      location: {
        state,
        search,
      },
    } = this.props;
    const backPath = `/devops/${state && state.appId ? "code-quality" : "reports"}${search}`;
    const { dateType, objectType } = this.state;
    const {
      getAllApps,
      getStartDate,
      getEndDate,
      getAppId,
      echartsLoading,
      isRefresh,
    } = ReportsStore;

    const content = (getAllApps && getAllApps.length ? <Fragment>
      <div className="c7n-codeQuality-select">
        <Select
          label={formatMessage({ id: 'chooseApp' })}
          className="c7n-select_250"
          defaultValue={getAppId}
          value={getAppId}
          optionFilterProp="children"
          filterOption={(input, option) => option.props.children.props.children.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
          filter
          onChange={this.handleAppSelect}
        >
          {
            _.map(getAllApps, ({ id, code, name }) => (
              <Option value={id} key={id}>
                <Tooltip title={code}>
                  <span>{name}</span>
                </Tooltip>
              </Option>))
          }
        </Select>
        <Select
          label={formatMessage({ id: 'report.code-quality.type' })}
          className="c7n-select_250 c7n-type-select"
          defaultValue={objectType}
          value={objectType}
          onChange={this.handleTypeSelect}
        >
          {
            _.map(["issue", "coverage", "duplicate"], item => (
              <Option value={item} key={item}>
                <FormattedMessage id={`report.code-quality.type.${item}`} />
              </Option>))
          }
        </Select>
        <TimePicker
          startTime={getStartDate}
          endTime={getEndDate}
          func={this.loadCharts}
          type={dateType}
          onChange={this.handleDateChoose}
          store={ReportsStore}
        />
      </div>
      <Spin spinning={echartsLoading}>
        <ReactEcharts
          className="c7n-codeQuality-charts"
          option={this.getOption()}
          notMerge
          lazyUpdate
        />
      </Spin>
    </Fragment> : <NoChart type="app" />);

    return (<Page
      className="c7n-region c7n-report-codeQuality-wrapper"
      service={[
        "devops-service.application.listByActive",
        "devops-service.application.getSonarQubeTable",
      ]}
    >
      <Header
        title={formatMessage({ id: 'report.code-quality.head' })}
        backPath={backPath}
      >
        <ChartSwitch
          history={history}
          current="code-quality"
        />
        <Button
          icon="refresh"
          onClick={this.handleRefresh}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </Header>
      <Content code="report.code-quality" values={{ name }}>
        {isRefresh ? <LoadingBar /> : content}
      </Content>
    </Page>);
  }
}

export default CodeQuality;
