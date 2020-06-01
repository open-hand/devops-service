import React, { useState, useEffect, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { FormattedMessage } from 'react-intl';
import { Page, Header, Content, Breadcrumb } from '@choerodon/boot';
import { Form, Select, Button, Tooltip } from 'choerodon-ui/pro';
import _ from 'lodash';
import moment from 'moment';
import ChartSwitch from '../Component/ChartSwitch';
import './BuildNumber.less';
import TimePicker from '../Component/TimePicker';
import NoChart from '../Component/NoChart';
import BuildTable from './BuildTable/BuildTable';
import LoadingBar from '../../../components/loading';
import BuildChart from './BuildChart';
import { useReportsStore } from '../stores';
import { useBuildNumberStore } from './stores';

const { Option } = Select;
const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

const BuildNumber = observer(() => {
  const {
    ReportsStore,
    ReportsStore: {
      getProRole,
      changeIsRefresh,
      setAllData,
      setBuildNumber,
      setStartTime,
      setEndTime,
      setAppId,
      setPageInfo,
      setStartDate,
      setEndDate,
      setAllApps,
      loadAllApps,
      pageInfo,
      getAllApps,
      appId,
      echartsLoading,
      isRefresh,
      getAppId,
      loadBuildNumber,
      loadBuildTable,
    },
    intl: { formatMessage },
    AppState,
    history,
    history: { location: { state, search } },
  } = useReportsStore();

  const {
    BuildNumberSelectDataSet,
  } = useBuildNumberStore();

  const record = BuildNumberSelectDataSet.current;

  const [dateType, setDateType] = useState('seven');

  useEffect(() => {
    changeIsRefresh(true);
    loadDatas();

    return () => {
      setAllData([]);
      setBuildNumber({});
      setStartTime(moment().subtract(6, 'days'));
      setEndTime(moment());
      setAppId(null);
      setPageInfo({ pageNum: 0, total: 0, pageSize: 10 });
      setStartDate();
      setEndDate();
      setAllApps([]);
    };
  }, []);

  useEffect(() => {
    record.set('buildNumberApps', appId);
  }, [appId]);

  /**
   * 加载数据
   */
  const loadDatas = () => {
    let historyAppId = null;
    if (state && state.appId) {
      historyAppId = state.appId;
    }
    const { id } = AppState.currentMenuType;
    loadAllApps(id).then((data) => {
      if (data && data.length) {
        let selectApp = data[0].id;
        if (historyAppId) {
          selectApp = historyAppId;
        }
        setAppId(selectApp);
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
    loadAllApps(id);
    loadCharts(pageInfo);
  };

  /**
   * 选择应用
   * @param value
   */
  const handleAppSelect = (value) => {
    setAppId(value);
    loadCharts();
  };

  const loadCharts = (pageInfoCurrent) => {
    const projectId = AppState.currentMenuType.id;
    const startTime = ReportsStore.getStartTime.format('YYYY-MM-DD HH:mm:ss');
    const appIdCurrent = ReportsStore.getAppId;
    const endTime = ReportsStore.getEndTime.format('YYYY-MM-DD HH:mm:ss');
    loadBuildNumber(projectId, appIdCurrent, startTime, endTime);
    if (pageInfoCurrent) {
      loadBuildTable(projectId, appIdCurrent, startTime, endTime, pageInfoCurrent.current, pageInfoCurrent.pageSize);
    } else {
      loadBuildTable(projectId, appIdCurrent, startTime, endTime);
    }
  };

  const handleDateChoose = (type) => {
    setDateType(type);
  };

  const { id, name, type, organizationId } = AppState.currentMenuType;
  const backPath = state && state.backPath ? state.backPath : 'reports';

  const content = (getAllApps.length ? <React.Fragment>
    <div className="c7n-buildNumber-select">
      <Form
        dataSet={BuildNumberSelectDataSet}
        className="c7n-app-select_247"
      >
        <Select
          name="buildNumberApps"
          // defaultValue={appId}
          // value={appId}
          optionFilterProp="children"
          filterOption={(input, option) => option.props.children.props.children.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
          filter
          searchable
          onChange={handleAppSelect}
          clearButton={false}
        >
          {
            _.map(getAllApps, (app, index) => (

              <Option value={app.id} key={index}>
                {app.name}
                {/* {app.name} */}
                {/* <Tooltip title={app.code}> */}
                {/*  <span className="c7n-app-select-tooltip"> */}
                {/*    {app.name} */}
                {/*  </span> */}
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
    <BuildChart ReportsStore={ReportsStore} echartsLoading={echartsLoading} height="400px" top="15%" languageType="report" />
    <BuildTable />
  </React.Fragment> : <NoChart getProRole={getProRole} type="app" />);

  return (<Page
    className="c7n-region c7n-ciPipeline"
    service={['choerodon.code.project.operation.chart.ps.build.times']}
  >
    <Header
      title={formatMessage({ id: 'report.build-number.head' })}
      backPath={`/charts${search}`}
    >
      <ChartSwitch
        history={history}
        current="build-number"
      />
      <Button
        icon="refresh"
        onClick={handleRefresh}
      >
        <FormattedMessage id="refresh" />
      </Button>
    </Header>
    <Breadcrumb title={formatMessage({ id: 'report.build-number.head' })} />
    <Content className="c7n-buildNumber-content">
      {isRefresh ? <LoadingBar display={isRefresh} /> : content}
    </Content>
  </Page>);
});
export default BuildNumber;
