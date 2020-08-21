import React, { Component, Fragment, useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Link, withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Page, Header, Content, Breadcrumb, Choerodon } from '@choerodon/boot';
import { Spin, Breadcrumb as Bread } from 'choerodon-ui';
import { Button, Tooltip, Form, Select } from 'choerodon-ui/pro';
import ReactEcharts from 'echarts-for-react';
import map from 'lodash/map';
import moment from 'moment';
import ChartSwitch from '../Component/ChartSwitch';
import TimePicker from '../Component/TimePicker';
import NoChart from '../Component/NoChart';
import LoadingBar from '../../../components/loading';
import { useReportsStore } from '../stores';
import { useCodeQualityStore } from './stores';

import './index.less';

const { Item } = Bread;
const OBJECT_TYPE = {
  issue: [
    { name: 'bugs', color: '#5266d4' },
    { name: 'codeSmells', color: '#2196f3' },
    { name: 'vulnerabilities', color: '#00bcd4' },
  ],
  coverage: [
    { name: 'linesToCover', color: '#2196f3' },
    { name: 'coverLines', color: '#00bcd4' },
  ],
  duplicate: [
    { name: 'nclocs', color: '#2196f3' },
    { name: 'duplicatedLines', color: '#00bcd4' },
  ],
};

const CodeQuality = withRouter(observer(() => {
  const {
    ReportsStore,
    ReportsStore: {
      isRefresh,
      getProRole,
    },
  } = useReportsStore();
  const {
    AppState: { currentMenuType: { projectId, name } },
    intl: { formatMessage },
    history,
    location: { search },
    detailDs,
    appServiceDs,
    chartsDs,
    backPath,
    permissions,
  } = useCodeQualityStore();

  const record = detailDs.current;

  const [dateType, setDateType] = useState('seven');

  useEffect(() => {
    ReportsStore.changeIsRefresh(true);

    return () => {
      ReportsStore.setStartTime(moment()
        .subtract(6, 'days'));
      ReportsStore.setEndTime(moment());
      ReportsStore.setStartDate();
      ReportsStore.setEndDate();
    };
  }, []);

  function loadCharts() {
    const { getStartTime, getEndTime, getAppId } = ReportsStore;
    const startTime = getStartTime.format('YYYY-MM-DD HH:mm:ss');
    const endTime = getEndTime.format('YYYY-MM-DD HH:mm:ss');
    chartsDs.setQueryParameter('startTime', startTime);
    chartsDs.setQueryParameter('endTime', endTime);
    chartsDs.query();
  }

  function handleRefresh() {
    appServiceDs.query();
    chartsDs.query();
  }

  /**
   * 选择时间
   * @param type
   */
  function handleDateChoose(value) {
    setDateType(value);
  }

  /**
   * 图表函数
   */
  function getOption() {
    const getCodeQuality = chartsDs.current ? chartsDs.current.toData() : {};
    const objectType = record.get('objectType');
    const series = [];
    const legend = [];
    const dates = getCodeQuality.dates || [];
    map(OBJECT_TYPE[objectType], ({ name: objectName, color }) => {
      if (getCodeQuality[objectName]) {
        series.push(
          {
            name: formatMessage({ id: `report.code-quality.${objectName}` }),
            type: 'line',
            symbol: 'circle',
            showSymbol: false,
            itemStyle: {
              color,
            },
            data: map(dates, (item, index) => [item, getCodeQuality[objectName][index]]),
          }
        );
        legend.push(
          {
            name: formatMessage({ id: `report.code-quality.${name}` }),
            icon: 'line',
          }
        );
      }
    });

    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'line',
        },
        backgroundColor: 'rgba(0,0,0,0.75)',
        textStyle: {
          color: '#fff',
        },
        extraCssText: '0px 2px 8px 0px rgba(0,0,0,0.12);padding:15px 17px',
        formatter(params) {
          const percent = (params[1].value[1] / (params[0].value[1] * 100)).toFixed(1);
          const list = map(params, ({ color, value, seriesName }) => (
            `<div>
              <span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:${color};"></span>
              <span>${seriesName}：${value[1]}</span>
            </div>`
          ));
          return `<div>
            <div><span>${formatMessage({ id: 'report.date' })}：${params[0].value[0].split('+')[0].replace(/T/g, ' ')}</span></div>
    ${objectType !== 'issue'
    ? `<div><span>${formatMessage({ id: `report.code-quality.type.${objectType}` })}：${percent}%</span></div>` : ''
}
            ${list.join('')}
          </div>`;
        },
      },
      legend: {
        data: legend,
        left: 'right',
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
        name: formatMessage({ id: objectType === 'issue' ? 'report.code-quality.number' : 'report.code-quality.rows' }),
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
        min: dates && dates.length ? null : 0,
        max: dates && dates.length ? null : 4,
        scale: true,
      },
      series,
    };
  }

  function renderAppServiceOption({ record: appServiceRecord, text }) {
    return (
      <Tooltip title={appServiceRecord.get('code')}>
        {text}
      </Tooltip>
    );
  }

  function getContent() {
    const {
      getStartDate,
      getEndDate,
    } = ReportsStore;
    return (appServiceDs.length ? <Fragment>
      <div className="c7n-codeQuality-select">
        <Form dataSet={detailDs} columns={2} style={{ width: '5.7rem' }}>
          <Select
            name="appServiceId"
            searchable
            optionRenderer={renderAppServiceOption}
            className="c7ncd-codeQuality-select-item"
            clearButton={false}
          />
          <Select
            name="objectType"
            clearButton={false}
            className="c7ncd-codeQuality-select-item"
          />
        </Form>
        <TimePicker
          startTime={getStartDate}
          endTime={getEndDate}
          func={loadCharts}
          type={dateType}
          onChange={handleDateChoose}
          store={ReportsStore}
        />
      </div>
      <Spin spinning={chartsDs.status === 'loading'}>
        <ReactEcharts
          className="c7n-codeQuality-charts"
          option={getOption()}
          notMerge
          lazyUpdate
        />
      </Spin>
    </Fragment> : <NoChart type="app" getProRole={getProRole} />);
  }

  if (!record) {
    return <LoadingBar display />;
  }

  return (<Page
    className="c7n-region c7n-report-codeQuality-wrapper"
    service={permissions}
  >
    <Header
      title={formatMessage({ id: 'report.code-quality.head' })}
      backPath={(function () {
        const params = new URLSearchParams(search);
        if (params.get('from') === 'ci') {
          params.delete('from');
          return `/devops/pipeline-manage?${params.toString()}`;
        } else {
          return `${backPath}${search}`;
        }
      }())}
    >
      <ChartSwitch
        history={history}
        current="code-quality"
      />
      <Button
        icon="refresh"
        onClick={handleRefresh}
      >
        <FormattedMessage id="refresh" />
      </Button>
    </Header>
    <Breadcrumb custom>
      <Item>{name}</Item>
      <Item>
        <Link to={`/charts${search}`}>
          {formatMessage({ id: 'report.bread.title' })}
        </Link>
      </Item>
      <Item>{formatMessage({ id: 'report.code-quality.head' })}</Item>
    </Breadcrumb>
    <Content>
      {isRefresh ? <LoadingBar display={isRefresh} /> : getContent()}
    </Content>
  </Page>);
}));

export default CodeQuality;
