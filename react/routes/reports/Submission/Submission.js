import React, { Fragment, useState, useEffect } from 'react';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Page, Header, Content, Breadcrumb } from '@choerodon/boot';
import { Select, Button, Form } from 'choerodon-ui/pro';
// import { Select, Button } from 'choerodon-ui';
import _ from 'lodash';
import moment from 'moment';
import ChartSwitch from '../Component/ChartSwitch';
import LineChart from './LineChart';
import CommitHistory from './CommitHistory';
import TimePicker from '../Component/TimePicker';
import NoChart from '../Component/NoChart';
import MaxTagPopover from '../Component/MaxTagPopover';
import './Submission.less';
import '../../main.less';
import LoadingBar from '../../../components/loading';
import { useReportsStore } from '../stores';
import { useSubmissionStore } from './stores';


/**
 * 将数据转为图表可用格式
 * @param data
 * @returns {{total, user: Array}}
 */


const { Option } = Select;

const Submission = observer(() => {
  const {
    intl: { formatMessage },
    AppState,
    ReportsStore,
    ReportsStore: {
      getProRole,
      getAllApps,
      getCommits,
      getCommitsRecord,
      getCommitLoading,
      getIsRefresh,
      getHistoryLoad,
      loadAllApps,
      loadCommits,
      loadCommitsRecord,
      changeIsRefresh,
    },
    history,
    history: {
      location: { state, search },
    },
  } = useReportsStore();

  const {
    SubmissionSelectDataSet,
  } = useSubmissionStore();

  const [appId, setAppId] = useState(null);
  const [page, setPage] = useState(1);
  const [dateType, setDateType] = useState('seven');
  const [index, setIndex] = useState(null);

  const record = SubmissionSelectDataSet.current;

  useEffect(() => {
    ReportsStore.changeIsRefresh(true);
    loadData();
    return () => {
      ReportsStore.setAllApps([]);
      ReportsStore.setCommits({});
      ReportsStore.setCommitsRecord([]);
      ReportsStore.setStartTime(moment().subtract(6, 'days'));
      ReportsStore.setEndTime(moment());
      ReportsStore.setStartDate(null);
      ReportsStore.setEndDate(null);
    };
  }, []);

  useEffect(() => {
    record.set('apps', appId);
  }, [appId]);

  const handleRefresh = () => loadData();

  function formatData(data) {
    const { totalCommitsDate, commitFormUserVOList } = data;
    const total = {};
    const user = [];
    if (totalCommitsDate && commitFormUserVOList) {
      // total.items = _.countBy(totalCommitsDate, item => item.slice(0, 10));
      total.items = totalCommitsDate.slice();
      total.count = totalCommitsDate.length;
      _.forEach(commitFormUserVOList, (item) => {
        const { name, imgUrl, commitDates, id } = item;
        const userTotal = {
          name,
          avatar: imgUrl,
        };
        userTotal.id = id;
        // userTotal.items = _.countBy(commitDates, cit => cit.slice(0, 10));
        userTotal.items = commitDates.slice();
        userTotal.count = commitDates.length;
        user.push(userTotal);
      });
    }

    return {
      total,
      user,
    };
  }

  /**
   * 应用选择
   * @param e
   */
  const handleSelect = (e) => {
    if (e.length === 0) {
      ReportsStore.setAllApps([]);
      ReportsStore.setCommits({});
      ReportsStore.setCommitsRecord([]);
    } else {
      const { id: projectId } = AppState.currentMenuType;
      const startTime = ReportsStore.getStartTime.format('YYYY-MM-DD HH:mm:ss');
      const endTime = ReportsStore.getEndTime.format('YYYY-MM-DD HH:mm:ss');
      setAppId(e);
      loadCommits(projectId, startTime, endTime, e);
      loadCommitsRecord(projectId, startTime, endTime, e, 1);
    }
  };

  const handlePageChange = (pageCurrent) => {
    const { id: projectId } = AppState.currentMenuType;
    const startTime = ReportsStore.getStartTime.format('YYYY-MM-DD HH:mm:ss');
    const endTime = ReportsStore.getEndTime.format('YYYY-MM-DD HH:mm:ss');
    setPage(pageCurrent);
    loadCommitsRecord(projectId, startTime, endTime, appId, pageCurrent);
  };

  const loadData = () => {
    let repoAppId = [];
    if (state && state.appId) {
      repoAppId = state.appId;
    }
    const { id: projectId } = AppState.currentMenuType;
    const startTime = ReportsStore.getStartTime.format('YYYY-MM-DD HH:mm:ss');
    const endTime = ReportsStore.getEndTime.format('YYYY-MM-DD HH:mm:ss');
    changeIsRefresh(true);
    loadAllApps(projectId).then((data) => {
      changeIsRefresh(false);
      const appData = data;
      if (appData.length) {
        let selectApp = appId || _.map(appData, (item) => item.id);
        if (!appId) {
          if (repoAppId.length) {
            selectApp = repoAppId;
          }
          setAppId(selectApp);
        }
        loadCommits(projectId, startTime, endTime, selectApp);
        loadCommitsRecord(projectId, startTime, endTime, selectApp, page);
      } else {
        ReportsStore.judgeRole(['choerodon.code.project.develop.app-service.ps.create']);
      }
    });
  };

  function handleRefreshChartByTimePicker() {
    const { id: projectId } = AppState.currentMenuType;
    const startTime = ReportsStore.getStartTime.format('YYYY-MM-DD HH:mm:ss');
    const endTime = ReportsStore.getEndTime.format('YYYY-MM-DD HH:mm:ss');
    loadCommits(projectId, startTime, endTime, appId);
    loadCommitsRecord(projectId, startTime, endTime, appId, index);
  }


  /**
   * 选择今天、近7天和近30天的选项，当使用DatePick的时候清空type
   * @param type 时间范围类型
   */
  const handleDateChoose = (type) => {
    setDateType(type);
  };

  const maxTagNode = (data, value) => (
    <MaxTagPopover dataSource={data} value={value} />
  );

  const backPath = state && state.backPath;
  const { id, name, type, organizationId } = AppState.currentMenuType;
  const { total, user } = formatData(getCommits);
  const options = _.map(getAllApps, (item) => (
    <Option key={item.id} value={item.id}>
      {item.name}
    </Option>
  ));
  const personChart = _.map(user, (item) => (
    <div key={item.id} className="c7n-report-submission-item">
      <LineChart
        languageType="report"
        loading={getCommitLoading}
        name={item.name || 'Unknown'}
        color="#ff9915"
        style={{ width: '100%', height: 176 }}
        data={item}
        start={ReportsStore.getStartTime}
        end={ReportsStore.getEndTime}
        hasAvatar
      />
    </div>
  ));

  const content = getAllApps.length ? (
    <Fragment>
      <div className="c7n-report-control c7n-report-select">
        <Form
          dataSet={SubmissionSelectDataSet}
          className="c7n-report-control-select"
        >
          <Select
            name="apps"
            searchable
            placeholder={formatMessage({ id: 'report.app.noselect' })}
            maxTagCount={3}
            // value={appId || []}
            maxTagPlaceholder={(omittedValues) => maxTagNode(getAllApps, omittedValues)}
            onChange={handleSelect}
            // optionFilterProp="children"
            // filter
          >
            {options}
          </Select>
        </Form>
        <TimePicker
          unlimit
          startTime={ReportsStore.getStartDate}
          endTime={ReportsStore.getEndDate}
          func={handleRefreshChartByTimePicker}
          store={ReportsStore}
          type={dateType}
          onChange={handleDateChoose}
        />
      </div>

      <div className="c7n-report-submission clearfix">
        <div className="c7n-report-submission-overview">
          <LineChart
            languageType="report"
            loading={getCommitLoading}
            name="提交情况"
            color="#4677dd"
            style={{ width: '100%', height: 276 }}
            data={total}
            hasAvatar={false}
            start={ReportsStore.getStartTime}
            end={ReportsStore.getEndTime}
          />
        </div>
        <div className="c7n-report-submission-history">
          <CommitHistory
            loading={getHistoryLoad}
            onPageChange={handlePageChange}
            dataSource={getCommitsRecord}
          />
        </div>
      </div>
      <div className="c7n-report-submission-wrap clearfix">{personChart}</div>
    </Fragment>
  ) : (
    <NoChart getProRole={getProRole} type="app" />
  );

  return (
    <Page
      className="c7n-region"
      service={['choerodon.code.project.operation.chart.ps.commit']}
    >
      <Header
        title={formatMessage({ id: 'report.submission.head' })}
        backPath={
          backPath
          || `/charts${search}`
        }
      >
        <ChartSwitch history={history} current="submission" />
        <Button icon="refresh" onClick={handleRefresh}>
          <FormattedMessage id="refresh" />
        </Button>
      </Header>
      <Breadcrumb title={formatMessage({ id: 'report.submission.head' })} />
      <Content>
        {getIsRefresh ? <LoadingBar display="getIsRefresh" /> : content}
      </Content>
    </Page>
  );
});

export default withRouter(injectIntl(Submission));
