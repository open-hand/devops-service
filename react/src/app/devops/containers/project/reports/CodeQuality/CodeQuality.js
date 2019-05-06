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
import { getAxis } from "../../../../utils";

import "./CodeQuality.scss";

const { Option } = Select;
const OBJECT_TYPE = {
  question: [
    { name: "bugs", color: "#5266d4" },
    { name: "codeSmells", color: "#2196f3" },
    { name: "vulnerabilities", color: "#00bcd4" },
  ],
  coverage: [
    { name: "coverageCodeRows", color: "#2196f3" },
    { name: "coverageRows", color: "#00bcd4" },
  ],
  duplications: [
    { name: "duplicationsCodeRows", color: "#2196f3" },
    { name: "duplicationsRows", color: "#00bcd4" },
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
      objectType: "question",
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
    } = this.props;
    ReportsStore.loadAllApps(projectId)
      .then((data) => {
        const appData = data && data.length ? _.filter(data, ['permission', true]) : [];
        if (appData.length) {
          ReportsStore.setAppId(appData[0].id);
          this.loadCharts();
        }
      });
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
    // ReportsStore.loadCodeQuality(projectId, getAppId, objectType, startTime, endTime);
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
    const { getStartTime, getEndTime } = ReportsStore;
    const { xAxis } = getAxis(getStartTime, getEndTime);

    const getCodeQuality = {
      bugs: [120, 132, 101, 134, 90, 230, 210],
      vulnerabilities: [220, 182, 191, 234, 290, 330, 310],
      codeSmells: [150, 232, 201, 154, 190, 330, 410],
      coverageCodeRows: [320, 332, 301, 334, 390, 330, 320],
      coverageRows: [820, 932, 901, 934, 1290, 1330, 1320],
      duplicationsCodeRows: [120, 732, 921, 534, 1090, 1110, 1320],
      duplicationsRows: [230, 532, 601, 734, 990, 1130, 1390],
    };
    const series = [];
    const legend = [];
    _.map(OBJECT_TYPE[objectType], ({ name, color }) => {
      series.push(
        {
          name: formatMessage({ id: `report.code-quality.${name}` }),
          type: 'line',
          symbol: 'none',
          itemStyle: {
            color: color,
          },
          data: getCodeQuality[name],
        }
      );
      legend.push(
        {
          name: formatMessage({ id: `report.code-quality.${name}` }),
          icon: "line",
        }
      )
    });

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
        padding: [10, 15],
        extraCssText:
          'box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2); border: 1px solid #ddd; border-radius: 0;',
      },
      legend: {
        data: legend,
        left: "right",
        itemGap: 40,
        itemWidth: 34,
      },
      grid: {
        left: '2%',
        right: '3%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
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
          formatter(value) {
            return `${value.substr(5).replace('-', '/')}`;
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
      },
      yAxis: {
        name: formatMessage({ id: objectType === "question" ? "report.code-quality.number" : "report.code-quality.rows" }),
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
        // min: () ? null : 0,
        // max: () ? null : 4,
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
        currentMenuType: {
          projectId,
          name,
          type,
          organizationId,
        },
      },
    } = this.props;
    const { dateType, objectType  } = this.state;
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
            _.map(["question", "coverage", "duplications"], item => (
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

      ]}
    >
      <Header
        title={formatMessage({ id: 'report.code-quality.head' })}
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
