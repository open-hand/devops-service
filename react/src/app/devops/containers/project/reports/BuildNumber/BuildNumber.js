import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Page, Header, Content, stores } from '@choerodon/boot';
import { Select, Button, Tooltip, Spin } from 'choerodon-ui';
import _ from 'lodash';
import moment from 'moment';
import ChartSwitch from '../Component/ChartSwitch';
import './BuildNumber.scss';
import TimePicker from '../Component/TimePicker';
import NoChart from '../Component/NoChart';
import BuildTable from './BuildTable/BuildTable';
import LoadingBar from '../../../../components/loadingBar/LoadingBar';
import BuildChart from './BuildChart';

const { AppState } = stores;
const { Option } = Select;
const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

@observer
class BuildNumber extends Component {
  constructor(props) {
    super(props);
    this.state = {
      dateType: 'seven',
    };
  }

  componentDidMount() {
    const { ReportsStore } = this.props;
    ReportsStore.changeIsRefresh(true);
    this.loadDatas();
  }

  componentWillUnmount() {
    const { ReportsStore } = this.props;
    ReportsStore.setAllData([]);
    ReportsStore.setBuildNumber({});
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
      history: { location: { state } },
    } = this.props;
    let historyAppId = null;
    if (state && state.appId) {
      historyAppId = state.appId;
    }
    const { id } = AppState.currentMenuType;
    ReportsStore.loadAllApps(id).then((data) => {
      const appData = data && data.length ? _.filter(data, ['permission', true]) : [];
      if (appData.length) {
        let selectApp = appData[0].id;
        if (historyAppId) {
          selectApp = historyAppId;
        }
        ReportsStore.setAppId(selectApp);
        this.loadCharts();
      }
    });
  };

  /**
   * 刷新
   */
  handleRefresh = () => {
    const { ReportsStore } = this.props;
    const { id } = AppState.currentMenuType;
    const { pageInfo } = ReportsStore;
    ReportsStore.loadAllApps(id);
    this.loadCharts(pageInfo);
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

  loadCharts = (pageInfo) => {
    const { ReportsStore } = this.props;
    const projectId = AppState.currentMenuType.id;
    const appId = ReportsStore.getAppId;
    const startTime = ReportsStore.getStartTime.format().split('T')[0].replace(/-/g, '/');
    const endTime = ReportsStore.getEndTime.format().split('T')[0].replace(/-/g, '/');
    ReportsStore.loadBuildNumber(projectId, appId, startTime, endTime);
    if (pageInfo) {
      ReportsStore.loadBuildTable(projectId, appId, startTime, endTime, pageInfo.current - 1, pageInfo.pageSize);
    } else {
      ReportsStore.loadBuildTable(projectId, appId, startTime, endTime);
    }
  };

  handleDateChoose = (type) => {
    this.setState({ dateType: type });
  };

  render() {
    const { intl: { formatMessage }, history, ReportsStore } = this.props;
    const { dateType } = this.state;
    const { id, name, type, organizationId } = AppState.currentMenuType;
    const { location: { state } } = history;
    const backPath = state && state.backPath ? state.backPath : "reports";
    const { getAllApps, appId, echartsLoading, isRefresh } = ReportsStore;

    const content = (getAllApps.length ? <React.Fragment>
      <div className="c7n-buildNumber-select">
        <Select
          label={formatMessage({ id: 'chooseApp' })}
          className="c7n-app-select_247"
          defaultValue={appId}
          value={appId}
          optionFilterProp="children"
          filterOption={(input, option) => option.props.children.props.children.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
          filter
          onChange={this.handleAppSelect}
        >
          {
            _.map(getAllApps, (app, index) => (
              <Option value={app.id} key={index}>
                <Tooltip title={app.code}>
                  <span className="c7n-app-select-tooltip">
                    {app.name}
                  </span>
                </Tooltip>
              </Option>))
          }
        </Select>
        <TimePicker
          startTime={ReportsStore.getStartDate}
          endTime={ReportsStore.getEndDate}
          func={this.loadCharts}
          type={dateType}
          onChange={this.handleDateChoose}
          store={ReportsStore}
        />
      </div>
      <BuildChart echartsLoading={echartsLoading} height="400px" top="15%" languageType="report" />
      <BuildTable />
    </React.Fragment> : <NoChart type="app" />);

    return (<Page
      className="c7n-region c7n-ciPipeline"
      service={[
        'devops-service.application.listByActive',
        'devops-service.devops-gitlab-pipeline.listPipelineFrequency',
        'devops-service.devops-gitlab-pipeline.pagePipeline',
        'devops-service.project-pipeline.cancel',
        'devops-service.project-pipeline.retry',
      ]}
    >
      <Header
        title={formatMessage({ id: 'report.build-number.head' })}
        backPath={`/devops/${backPath}?type=${type}&id=${id}&name=${name}&organizationId=${organizationId}`}
      >
        <ChartSwitch
          history={history}
          current="build-number"
        />
        <Button
          icon="refresh"
          onClick={this.handleRefresh}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </Header>
      <Content code="report.build-number" values={{ name }} className="c7n-buildNumber-content">
        {isRefresh ? <LoadingBar /> : content}
      </Content>
    </Page>);
  }
}

export default injectIntl(BuildNumber);
