import React, { Component } from 'react';
import { injectIntl } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { Spin } from 'choerodon-ui';
import ReactEcharts from 'echarts-for-react';
import _ from 'lodash';
import ReportsStore from '../../../../stores/project/reports';
import '../../ciPipelineManage/ciPipelineHome/CiPipelineHome.scss';
import { getAxis } from '../../../../utils';

class BuildTable extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
    };
  }

  getOption() {
    const { intl: { formatMessage }, top, bottom, languageType } = this.props;
    const { createDates, pipelineFrequencys, pipelineSuccessFrequency, pipelineFailFrequency } = ReportsStore.getBuildNumber;
    const val = [{ name: `${formatMessage({ id: `${languageType}.build-number.fail` })}` }, { name: `${formatMessage({ id: `${languageType}.build-number.success` })}` }, { name: `${formatMessage({ id: `${languageType}.build-number.total` })}` }];
    val[0].value = _.reduce(pipelineFailFrequency, (sum, n) => sum + n, 0);
    val[1].value = _.reduce(pipelineSuccessFrequency, (sum, n) => sum + n, 0);
    val[2].value = _.reduce(pipelineFrequencys, (sum, n) => sum + n, 0);
    const startTime = ReportsStore.getStartTime;
    const endTime = ReportsStore.getEndTime;
    const { xAxis, yAxis } = getAxis(startTime, endTime, createDates, { pipelineFailFrequency, pipelineSuccessFrequency, pipelineFrequencys });
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
        formatter(params) {
          if (params[0].value || params[1].value) {
            const total = params[0].value + params[1].value;
            return `<div>
              <div>${formatMessage({ id: `${languageType === 'report' ? 'branch' : 'dashboard'}.issue.date` })}：${params[1].name}</div>
              <div><span class="c7n-echarts-tooltip" style="background-color:${params[1].color};"></span>${formatMessage({ id: `${languageType}.build-number.build` })}${params[1].seriesName}：${params[1].value}</div>
              <div><span class="c7n-echarts-tooltip" style="background-color:${params[0].color};"></span>${formatMessage({ id: `${languageType}.build-number.build` })}${params[0].seriesName}：${params[0].value}</div>
              <div>${formatMessage({ id: `${languageType}.build-number.build` })}${formatMessage({ id: `${languageType}.build-number.total` })}：${total}</div>
              <div>${formatMessage({ id: `${languageType}.build-number.build` })}${formatMessage({ id: `${languageType}.build-number.success.rate` })}：${((params[0].value / total) * 100).toFixed(2)}%</div>
            <div>`;
          }
        },
      },
      legend: {
        left: 'right',
        itemWidth: 14,
        itemGap: 20,
        padding: [0, 5, 5, 5],
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
        bottom: bottom || '3%',
        top: `${top}`,
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
        name: `${formatMessage({ id: `${languageType}.build-number.yAxis` })}`,
        type: 'value',

        nameTextStyle: {
          fontSize: 13,
          color: '#000',
          padding: [0, 5, 2, 5],
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
        min: (yAxis.pipelineFrequencys && yAxis.pipelineFrequencys.length) ? null : 0,
        max: (yAxis.pipelineFrequencys && yAxis.pipelineFrequencys.length) ? null : 4,
      },
      series: [
        {
          name: `${formatMessage({ id: `${languageType}.build-number.success` })}`,
          type: 'bar',
          barWidth: '40%',
          itemStyle: {
            color: '#00BFA5',
            emphasis: {
              shadowBlur: 10,
              shadowColor: 'rgba(0,0,0,0.20)',
            },
          },
          stack: 'total',
          data: yAxis.pipelineSuccessFrequency,
        },
        {
          name: `${formatMessage({ id: `${languageType}.build-number.fail` })}`,
          type: 'bar',
          barWidth: '40%',
          itemStyle: {
            color: '#F44336',
            emphasis: {
              shadowBlur: 10,
              shadowColor: 'rgba(0,0,0,0.20)',
            },
          },
          stack: 'total',
          data: yAxis.pipelineFailFrequency,
        },
        {
          name: `${formatMessage({ id: `${languageType}.build-number.total` })}`,
          type: 'bar',
          color: '#FFF',
          stack: 'total',
        },
      ],
    };
  }

  render() {
    const { echartsLoading, height } = this.props;
    return (
      <Spin spinning={echartsLoading}>
        <ReactEcharts style={{ height }} option={this.getOption()} />
      </Spin>
    );
  }
}

export default withRouter(injectIntl(BuildTable));
