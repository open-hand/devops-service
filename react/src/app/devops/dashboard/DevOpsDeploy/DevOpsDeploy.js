import React, { Component, Fragment } from 'react';
import { Link, withRouter } from 'react-router-dom';
import { DashBoardNavBar, stores, axios } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react';
import { Select, Spin } from 'choerodon-ui';
import _ from 'lodash';
import ReactEcharts from 'echarts-for-react';
import moment from 'moment';
import '../common.scss';
import './index.scss';
import MaxTagPopover from '../../containers/project/reports/Component/MaxTagPopover';
import { getAxis, handleProptError } from '../../utils';

const { AppState } = stores;
const { Option } = Select;

@observer
class DevOpsDeploy extends Component {
  constructor(props) {
    super(props);
    this.state = {
      app: [],
      env: [],
      appId: null,
      envIds: [],
      dateArr: [],
      successArr: [],
      failArr: [],
      allArr: [],
      loading: true,
      noSelect: false,
    };
  }

  componentDidMount() {
    this.loadData();
  }

  loadData() {
    const { projectId } = AppState.currentMenuType;
    const loadApp = () => axios.get(`/devops/v1/projects/${projectId}/apps/list_all`);

    const loadEnv = () => axios.get(`devops/v1/projects/${projectId}/envs?active=true`);

    axios.all([loadApp(), loadEnv()])
      .then(axios.spread((app, env) => {
        const appRes = handleProptError(app);
        const envData = handleProptError(env);
        const envRes = envData && envData.length ? _.filter(envData, ['permission', true]) : [];
        if (appRes.length && envRes.length) {
          this.setState({
            app: appRes,
            env: envRes,
            appId: appRes[0].id,
            envIds: [envRes[0].id],
          });
          this.loadCharts(appRes[0].id, [envRes[0].id]);
        } else {
          this.setState({ loading: false, noSelect: true });
        }
      }));
  }

  /**
   * 加载图表数据
   */
  loadCharts = (id, ids) => {
    const projectId = AppState.currentMenuType.id;
    const startTime = moment().subtract(6, 'days').format().split('T')[0].replace(/-/g, '/');
    const endTime = moment().format().split('T')[0].replace(/-/g, '/');
    const appId = id === 'all' ? [] : id;
    this.setState({
      loading: true,
    });
    axios.post(`devops/v1/projects/${projectId}/app_instances/env_commands/frequency?appId=${appId}&endTime=${endTime}&startTime=${startTime}`, JSON.stringify(ids))
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setState({
            dateArr: res.creationDates,
            successArr: res.deploySuccessFrequency,
            failArr: res.deployFailFrequency,
            allArr: res.deployFrequencys,
            loading: false,
          });
        } else {
          this.setState({ loading: false });
        }
      });
  };

  /**
   * 选择应用
   * @param id 应用ID
   */
  handleAppSelect = (id) => {
    this.setState({
      appId: id,
    });
    this.loadCharts(id, this.state.envIds);
  };

  /**
   * 选择环境
   * @param ids 环境ID
   */
  handleEnvSelect = (ids) => {
    this.setState({
      envIds: ids,
    });
    this.loadCharts(this.state.appId, ids);
  };

  /**
   * 渲染图表
   * @returns {*}
   */
  getOption() {
    const { intl: { formatMessage } } = this.props;
    const startTime = moment().subtract(6, 'days');
    const endTime = moment();
    const { dateArr, successArr, failArr, allArr } = this.state;
    const val = [{ name: `${formatMessage({ id: 'dashboard.build-number.fail' })}` }, { name: `${formatMessage({ id: 'dashboard.build-number.success' })}` }, { name: `${formatMessage({ id: 'dashboard.build-number.total' })}` }];
    val[0].value = _.reduce(failArr, (sum, n) => sum + n, 0);
    val[1].value = _.reduce(successArr, (sum, n) => sum + n, 0);
    val[2].value = _.reduce(allArr, (sum, n) => sum + n, 0);
    const { xAxis, yAxis } = getAxis(startTime, endTime, dateArr || [], { successArr, failArr, allArr });
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
        top: 'bottom',
        extraCssText:
          'box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2); border: 1px solid #ddd; border-radius: 0;',
        formatter(params) {
          if (params[1].value || params[0].value) {
            const total = params[0].value + params[1].value;
            return `<div>
                <div>${formatMessage({ id: 'dashboard.issue.date' })}：${params[1].name}</div>
                <div><span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:${params[1].color};"></span>${formatMessage({ id: 'dashboard.deploy' })}${params[1].seriesName}：${params[1].value}</div>
                <div><span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:${params[0].color};"></span>${formatMessage({ id: 'dashboard.deploy' })}${params[0].seriesName}：${params[0].value}</div>
                <div>${formatMessage({ id: 'dashboard.deploy' })}${formatMessage({ id: 'dashboard.build-number.total' })}：${total}</div>
                <div>${formatMessage({ id: 'dashboard.deploy' })}${formatMessage({ id: 'dashboard.build-number.success.rate' })}：${((params[0].value / total) * 100).toFixed(2)}%</div>
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
        top: '10%',
        left: '2%',
        right: '3%',
        bottom: '4%',
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
        name: `${formatMessage({ id: 'dashboard.build-number.yAxis' })}`,
        min: yAxis.allArr.length ? null : 0,
        max: yAxis.allArr.length ? null : 4,
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
          name: `${formatMessage({ id: 'dashboard.build-number.success' })}`,
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
          name: `${formatMessage({ id: 'dashboard.build-number.fail' })}`,
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
          name: `${formatMessage({ id: 'dashboard.build-number.total' })}`,
          type: 'bar',
          color: 'transparent',
          stack: 'total',
        },
      ],
    };
  }

  maxTagNode = (data, value) => <MaxTagPopover dataSource={data} value={value} />;

  getContent = () => {
    const { loading } = this.state;
    if (loading) {
      return (<div className="c7ncd-dashboard-loading"><Spin /></div>);
    }
    return (<ReactEcharts
      option={this.getOption()}
      notMerge
      lazyUpdate
      style={{ height: '300px', width: '100%' }}
    />);
  };

  render() {
    const { id: projectId, name: projectName, organizationId, type } = AppState.currentMenuType;
    const { app, appId, env, envIds, noSelect } = this.state;
    const { intl: { formatMessage } } = this.props;
    const appDom = app.length ? _.map(app, d => (<Option key={d.id} value={d.id}>{d.name}</Option>)) : null;
    const envDom = env.length ? _.map(env, d => (<Option key={d.id} value={d.id}>{d.name}</Option>)) : null;

    return (<Fragment>
      <div className="c7ncd-db-panel">
        <Select
          notFoundContent={formatMessage({ id: 'dashboard.noEnv' })}
          placeholder={formatMessage({ id: 'dashboard.env' })}
          value={envIds}
          className={`c7n-select_100 env-multi ${envIds.length > 1 ? 'env-multi_150' : ''} ${envIds.length === 0 ? 'c7n-select-noSelect' : ''}`}
          mode="multiple"
          maxTagCount={1}
          onChange={this.handleEnvSelect}
          maxTagPlaceholder={this.maxTagNode.bind(this, env)}
        >
          {envDom}
        </Select>
        <Select
          dropdownMatchSelectWidth
          notFoundContent={formatMessage({ id: 'dashboard.noApp' })}
          placeholder={formatMessage({ id: 'dashboard.environment.select' })}
          value={appDom ? appId : null}
          className={`c7n-select_100 ${noSelect ? 'c7n-select-noSelect' : ''}`}
          onChange={this.handleAppSelect}
        >
          {appDom}
          {appDom ? <Option key="all" value="all">{formatMessage({ id: 'dashboard.allApp' })}</Option> : null}
        </Select>
        <div className="c7ncd-db-panel-size">{this.getContent()}</div>
      </div>
      <DashBoardNavBar>
        <Link
          to={{
            pathname: '/devops/reports/deploy-times',
            search: `?type=${type}&id=${projectId}&name=${encodeURIComponent(projectName)}&organizationId=${organizationId}`,
            state: {
              appId,
              envIds,
            },
          }}
        >
          <FormattedMessage id="dashboard.deployment" />
        </Link>
      </DashBoardNavBar>
    </Fragment>);
  }
}

export default withRouter(injectIntl(DevOpsDeploy));
