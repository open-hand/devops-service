import React, { Component, useState, useEffect } from 'react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Page, Header, Content, Breadcrumb } from '@choerodon/boot';
import { Select, Button, Table, Spin, Form } from 'choerodon-ui/pro';
import ReactEcharts from 'echarts-for-react';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import filter from 'lodash/filter';
import moment from 'moment';
import StatusTags from '../../../components/status-tag';
import LoadingBar from '../../../components/loading';
import MouserOverWrapper from '../../../components/MouseOverWrapper';
import ChartSwitch from '../Component/ChartSwitch';
import TimePicker from '../Component/TimePicker';
import NoChart from '../Component/NoChart';
import MaxTagPopover from '../Component/MaxTagPopover';
import { useDeployDurationStore } from './stores';
import { useReportsStore } from '../stores';

import './DeployDuration.less';

const { Column } = Table;

const COLOR = ['50,198,222', '116,59,231', '87,170,248', '255,177,0', '237,74,103'];
const LENGEND = [
  'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNiIgaGVpZ2h0PSIyNiIgdmlld0JveD0iMCAwIDI2IDI2Ij4KICA8Y2lyY2xlIGN4PSI0OTkiIGN5PSI2MiIgcj0iMTIiIGZpbGw9IiMzMkM2REUiIGZpbGwtb3BhY2l0eT0iLjYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgc3Ryb2tlPSIjMzJDNkRFIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtNDg2IC00OSkiLz4KPC9zdmc+Cg==',
  'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNiIgaGVpZ2h0PSIyNiIgdmlld0JveD0iMCAwIDI2IDI2Ij4KICA8Y2lyY2xlIGN4PSI0NDkiIGN5PSI1NyIgcj0iMTIiIGZpbGw9IiM3NDNCRTciIGZpbGwtb3BhY2l0eT0iLjYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgc3Ryb2tlPSIjNzQzQkU3IiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtNDM2IC00NCkiLz4KPC9zdmc+Cg==',
  'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNiIgaGVpZ2h0PSIyNiIgdmlld0JveD0iMCAwIDI2IDI2Ij4KICA8Y2lyY2xlIGN4PSI0OTkiIGN5PSI1NyIgcj0iMTIiIGZpbGw9IiM1N0FBRjgiIGZpbGwtb3BhY2l0eT0iLjYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgc3Ryb2tlPSIjNTdBQUY4IiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtNDg2IC00NCkiLz4KPC9zdmc+Cg==',
  'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB3aWR0aD0iMjYiIGhlaWdodD0iMjYiIHZpZXdCb3g9IjAgMCAyNiAyNiI+CiAgPGRlZnM+CiAgICA8Y2lyY2xlIGlkPSI1LWEiIGN4PSI0OTkiIGN5PSI1NyIgcj0iMTIiLz4KICA8L2RlZnM+CiAgPGcgZmlsbD0ibm9uZSIgZmlsbC1ydWxlPSJldmVub2RkIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtNDg2IC00NCkiPgogICAgPHVzZSBmaWxsPSIjRkZCMTAwIiBmaWxsLW9wYWNpdHk9Ii42IiB4bGluazpocmVmPSIjNS1hIiBzdHlsZT0ibWl4LWJsZW5kLW1vZGU6c2F0dXJhdGlvbiIvPgogICAgPHVzZSBzdHJva2U9IiNGRkIxMDAiIHhsaW5rOmhyZWY9IiM1LWEiLz4KICA8L2c+Cjwvc3ZnPgo=',
  'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNiIgaGVpZ2h0PSIyNiIgdmlld0JveD0iMCAwIDI2IDI2Ij4KICA8Y2lyY2xlIGN4PSI0OTkiIGN5PSI1NyIgcj0iMTIiIGZpbGw9IiNFRDRBNjciIGZpbGwtb3BhY2l0eT0iLjYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgc3Ryb2tlPSIjRUQ0QTY3IiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtNDg2IC00NCkiLz4KPC9zdmc+Cg==',
];

const DeployDuration = observer(() => {
  const {
    ReportsStore,
    ReportsStore: {
      isRefresh,
      getProRole,
    },
  } = useReportsStore();
  const {
    intl: { formatMessage },
    detailDs,
    appServiceDs,
    chartsDs,
    permissions,
    envDs,
    tableDs,
    history,
    location: { search },
  } = useDeployDurationStore();
  const [dateArr, setDateArr] = useState([]);
  const [appArr, setAppArr] = useState([]);
  const [seriesArr, setSeriesArr] = useState([]);
  const [dateType, setDateType] = useState('seven');

  useEffect(() => {
    ReportsStore.changeIsRefresh(true);

    return () => {
      ReportsStore.setStartTime(moment().subtract(6, 'days'));
      ReportsStore.setEndTime(moment());
      ReportsStore.setStartDate();
      ReportsStore.setEndDate();
    };
  }, []);

  useEffect(() => {
    if (!chartsDs.current) {
      return;
    }
    const res = chartsDs.current.toData();
    if (res) {
      const { creationDates, deployAppVOS } = res;
      setDateArr(creationDates);
      const newSeriesArr = [];
      const newAppArr = [];
      map(deployAppVOS, (v, index) => {
        const series = {
          name: v.appServiceName,
          symbolSize: 24,
          itemStyle: {
            color: `rgba(${COLOR[index]}, 0.6)`,
            borderColor: `rgb(${COLOR[index]})`,
          },
          data: map(v.deployDetailVOS, (c) => Object.values(c)),
          type: 'scatter',
        };
        const obj = {};
        obj.name = v.appServiceName;
        obj.icon = `image://${LENGEND[index]}`;
        newSeriesArr.push(series);
        newAppArr.push(obj);
      });
      setSeriesArr(newSeriesArr);
      setAppArr(newAppArr);
    }
  }, [chartsDs.current]);

  function handleRefresh() {
    envDs.query();
    appServiceDs.query();
    chartsDs.query();
    tableDs.query();
  }

  /**
   * 加载数据
   */
  function loadData() {
    const startTime = ReportsStore.getStartTime.format('YYYY-MM-DD HH:mm:ss');
    const endTime = ReportsStore.getEndTime.format('YYYY-MM-DD HH:mm:ss');
    chartsDs.setQueryParameter('startTime', startTime);
    chartsDs.setQueryParameter('endTime', endTime);
    tableDs.setQueryParameter('startTime', startTime);
    tableDs.setQueryParameter('endTime', endTime);
    chartsDs.query();
    tableDs.query();
  }

  /**
   * 图表函数
   * @returns {*}
   */
  function getOption() {
    return {
      legend: {
        data: appArr,
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
        backgroundColor: 'rgba(0,0,0,0.75)',
        textStyle: {
          color: '#fff',
        },
        extraCssText: '0px 2px 8px 0px rgba(0,0,0,0.12);padding:15px 17px',
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
        min: seriesArr.length ? null : 0,
        max: seriesArr.length ? null : 4,
        scale: true,
      },
      series: seriesArr,
    };
  }

  function renderTableStatus({ value, record }) {
    const error = record.get('status');
    return (
      <StatusTags
        name={formatMessage({ id: value })}
        colorCode={value || ''}
        error={error}
      />
    );
  }

  function renderText({ value }) {
    return (
      <MouserOverWrapper
        text={value}
        width={0.2}
      >
        {value}
      </MouserOverWrapper>
    );
  }

  function handleDateChoose(type) {
    setDateType(type);
  }

  function maxTagNode(value) {
    return <MaxTagPopover dataSource={appServiceDs.toData()} value={value} />;
  }

  function getContent() {
    const envData = envDs.toData();
    const envs = filter(envData, ['permission', true]);
    return (envs && envs.length ? <React.Fragment>
      <div className="c7n-report-screen c7n-report-select">
        <Form dataSet={detailDs} columns={3} style={{ paddingRight: '.2rem' }}>
          <Select
            name="envId"
            searchable
            colSpan={1}
            clearButton={false}
          />
          <Select
            name="appServiceIds"
            searchable
            maxTagCount={3}
            maxTagPlaceholder={maxTagNode}
            showCheckAll={false}
            colSpan={2}
          />
        </Form>
        <TimePicker
          startTime={ReportsStore.getStartDate}
          endTime={ReportsStore.getEndDate}
          func={loadData}
          type={dateType}
          onChange={handleDateChoose}
          store={ReportsStore}
        />
      </div>
      <div className="c7n-report-content">
        <Spin spinning={chartsDs.status === 'loading'}>
          <ReactEcharts
            option={getOption()}
            notMerge
            lazyUpdate
            style={{ height: '350px', width: '100%' }}
          />
        </Spin>
      </div>
      <div className="c7n-report-table">
        <Table dataSet={tableDs} queryBar="none">
          <Column name="status" renderer={renderTableStatus} />
          <Column name="creationDate" />
          <Column name="appServiceInstanceCode" renderer={renderText} />
          <Column name="appServiceName" renderer={renderText} />
          <Column name="appServiceVersion" renderer={renderText} />
          <Column name="lastUpdatedName" />
        </Table>
      </div>
    </React.Fragment> : <NoChart type="env" getProRole={getProRole} />);
  }

  if (!detailDs.current) {
    return <LoadingBar display />;
  }

  return (<Page
    className="c7n-region"
    service={permissions}
  >
    <Header
      title={formatMessage({ id: 'report.deploy-duration.head' })}
      backPath={`/charts${search}`}
    >
      <ChartSwitch
        history={history}
        current="deploy-duration"
      />
      <Button
        icon="refresh"
        onClick={handleRefresh}
      >
        <FormattedMessage id="refresh" />
      </Button>
    </Header>
    <Breadcrumb title={formatMessage({ id: 'report.deploy-duration.head' })} />
    <Content>
      {isRefresh ? <LoadingBar display={isRefresh} /> : getContent()}
    </Content>
  </Page>);
});

export default DeployDuration;
