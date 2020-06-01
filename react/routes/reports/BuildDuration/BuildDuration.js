import React, { useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Page, Header, Content, Breadcrumb } from '@choerodon/boot';
import { Form, Select } from 'choerodon-ui/pro';
import { Button, Tooltip, Spin } from 'choerodon-ui';
import ReactEcharts from 'echarts-for-react';
import _ from 'lodash';
import moment from 'moment';
import LoadingBar from '../../../components/loading';
import ChartSwitch from '../Component/ChartSwitch';
import TimePicker from '../Component/TimePicker';
import NoChart from '../Component/NoChart';
import BuildTable from '../BuildNumber/BuildTable/BuildTable';
import './BuildDuration.less';
import { useReportsStore } from '../stores';
import { useBuildDurationStore } from './stores';


const { Option } = Select;
const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

const BuildDuration = observer(() => {
  const {
    AppState,
    ReportsStore,
    history,
    intl: { formatMessage },
    location: { search },
  } = useReportsStore();

  const {
    BuildDurationSelectDataSet,
  } = useBuildDurationStore();

  const record = BuildDurationSelectDataSet.current;

  const [dateType, setDateType] = useState('seven');

  useEffect(() => {
    ReportsStore.changeIsRefresh(true);
    loadDatas();

    return () => {
      ReportsStore.setAllData([]);
      ReportsStore.setBuildDuration({});
      ReportsStore.setStartTime(moment().subtract(6, 'days'));
      ReportsStore.setEndTime(moment());
      ReportsStore.setAppId(null);
      ReportsStore.setPageInfo({ pageNum: 1, total: 0, pageSize: 10 });
      ReportsStore.setStartDate();
      ReportsStore.setEndDate();
      ReportsStore.setAllApps([]);
    };
  }, []);

  useEffect(() => {
    record.set('buildDurationApps', ReportsStore.getAppId);
  }, [ReportsStore.getAppId]);

  /**
   * 加载数据
   */
  const loadDatas = () => {
    const { id } = AppState.currentMenuType;
    ReportsStore.loadAllApps(id).then((data) => {
      if (data && data.length) {
        ReportsStore.setAppId(data[0].id);
        loadCharts();
      } else {
        ReportsStore.judgeRole(['choerodon.code.project.develop.app-service.ps.create']);
      }
    });
  };

  /**
   * 刷新
   */
  const handleRefresh = () => {
    const { id } = AppState.currentMenuType;
    const { pageInfo } = ReportsStore;
    ReportsStore.loadAllApps(id);
    loadCharts(pageInfo);
  };

  /**
   * 选择应用
   * @param value
   */
  const handleAppSelect = (value) => {
    ReportsStore.setAppId(value);
    loadCharts();
  };

  /**
   * 图表函数
   */
  function getOption() {
    const { pipelineTime, refs, versions, createDates } = ReportsStore.getBuildDuration;
    const averageDuration = [];
    averageDuration.length = pipelineTime && pipelineTime.length ? pipelineTime.length : 0;
    const ava = pipelineTime && pipelineTime.length ? ((_.reduce(pipelineTime, (sum, n) => sum + parseFloat(n), 0)) / pipelineTime.length) : 0;
    _.fill(averageDuration, ava);
    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'none',
        },
        backgroundColor: 'rgba(0,0,0,0.75)',
        textStyle: {
          color: '#fff',
        },
        padding: [10, 15],
        extraCssText:
          'box-shadow: 0px 2px 8px 0px rgba(0,0,0,0.12);padding:15px 17px',
        formatter(params, ticket) {
          const version = versions[params[0].dataIndex] ? `${versions[params[0].dataIndex]}` : `${formatMessage({ id: 'report.build-duration.noversion' })}`;
          let time = params[0].value;
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
            <div>${formatMessage({ id: 'ist.time' })}：${createDates[params[0].dataIndex]}</div>
            <div>${formatMessage({ id: 'network.column.version' })}：${version}</div>
            <div>${formatMessage({ id: 'report.build-duration.duration' })}：${time}</div>
          </div>`;
        },
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
          rotate: 40,
          formatter(value) {
            return `${value.substr(0, value.indexOf('-') + 5)}`;
          },
        },
        splitLine: {
          lineStyle: {
            color: ['#eee'],
            width: 1,
            type: 'solid',
          },
        },
        data: refs,
      },
      yAxis: {
        name: `${formatMessage({ id: 'minTime' })}`,
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
          margin: 19.3,
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
        min: (pipelineTime && pipelineTime.length) ? null : 0,
        max: (pipelineTime && pipelineTime.length) ? null : 4,
      },
      series: [
        {
          type: 'bar',
          barWidth: '30%',
          itemStyle: {
            color: 'rgba(77, 144, 254, 0.60)',
            borderColor: '#4D90FE',
            emphasis: {
              shadowBlur: 10,
              shadowColor: 'rgba(0,0,0,0.20)',
            },
          },
          data: pipelineTime,
        },
        {
          type: 'line',
          symbol: 'none',
          lineStyle: {
            color: 'rgba(0, 0, 0, 0.36)',
            width: 2,
            type: 'dashed',
            border: '1px solid #4D90FE',
          },
          data: averageDuration,
        },
      ],
    };
  }

  const loadCharts = (pageInfo) => {
    const projectId = AppState.currentMenuType.id;
    const appId = ReportsStore.getAppId;
    const startTime = ReportsStore.getStartTime.format('YYYY-MM-DD HH:mm:ss');
    const endTime = ReportsStore.getEndTime.format('YYYY-MM-DD HH:mm:ss');
    ReportsStore.loadBuildDuration(projectId, appId, startTime, endTime);
    if (pageInfo) {
      ReportsStore.loadBuildTable(projectId, appId, startTime, endTime, pageInfo.current, pageInfo.pageSize);
    } else {
      ReportsStore.loadBuildTable(projectId, appId, startTime, endTime);
    }
  };

  const handleDateChoose = (type) => {
    setDateType(type);
  };

  const { id, name, type, organizationId } = AppState.currentMenuType;
  const { getAllApps, appId, echartsLoading, isRefresh } = ReportsStore;

  const content = (getAllApps && getAllApps.length ? <React.Fragment>
    <div className="c7n-buildDuration-select">
      <Form
        dataSet={BuildDurationSelectDataSet}
        className="c7n-app-select_247"

      >
        <Select
          name="buildDurationApps"
          // defaultValue={appId}
          // value={appId}
          searchable
          optionFilterProp="children"
          filterOption={(input, option) => option.props.children.props.children.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
          filter
          onChange={handleAppSelect}
          clearButton={false}
        >
          {
            _.map(getAllApps, (app, index) => (
              <Option value={app.id} key={index}>
                {app.name}
                {/* <Tooltip title={app.code}> */}
                {/* <span className="c7n-app-select-tooltip"> */}
                {/*  */}
                {/* </span> */}
                {/* </Tooltip> */}
              </Option>))
          }
        </Select>
      </Form>
      <TimePicker
        startTime={ReportsStore.getStartDate}
        endTime={ReportsStore.getEndDate}
        func={loadCharts}
        type={dateType}
        onChange={handleDateChoose}
        store={ReportsStore}
      />
    </div>
    <Spin spinning={echartsLoading}>
      <ReactEcharts className="c7n-buildDuration-echarts" option={getOption()} />
    </Spin>
    <BuildTable />
  </React.Fragment> : <NoChart getProRole={ReportsStore.getProRole} type="app" />);

  return (<Page
    className="c7n-region c7n-ciPipeline"
    service={['choerodon.code.project.operation.chart.ps.build.duration']}
  >
    <Header
      title={formatMessage({ id: 'report.build-duration.head' })}
      backPath={`/charts${search}`}
    >
      <ChartSwitch
        history={history}
        current="build-duration"
      />
      <Button
        icon="refresh"
        onClick={handleRefresh}
      >
        <FormattedMessage id="refresh" />
      </Button>
    </Header>
    <Breadcrumb title={formatMessage({ id: 'report.build-duration.head' })} />
    <Content className="c7n-buildDuration-content">
      {isRefresh ? <LoadingBar display={isRefresh} /> : content}
    </Content>
  </Page>);
});

export default injectIntl(BuildDuration);
