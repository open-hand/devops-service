import React, { Component, Fragment } from 'react';
import { Link, withRouter } from 'react-router-dom';
import { axios, DashBoardNavBar, stores } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react';
import { Select, Spin, Tooltip, Avatar, Button } from 'choerodon-ui';
import _ from 'lodash';
import '../common.scss';
import './index.scss';
import ReportsStore from '../../stores/project/reports';
import DevPipelineStore from '../../stores/project/devPipeline';
import TimePopover from '../../components/timePopover';
import { handleProptError } from '../../utils';

const { AppState } = stores;
const { Option } = Select;

@observer
class DevOpsBranch extends Component {
  constructor(props) {
    super(props);
    this.state = {
      appId: null,
      loading: true,
      noSelect: false,
      branchList: [],
    };
  }

  componentDidMount() {
    const { id } = AppState.currentMenuType;
    ReportsStore.loadAllApps(id).then((data) => {
      const appData = data && data.length ? _.filter(data, ['permission', true]) : [];
      if (appData.length) {
        DevPipelineStore.setRecentApp(appData[0].id);
        this.setState({ appId: appData[0].id });
        this.loadData();
      } else {
        this.setState({ loading: false, noSelect: true });
      }
    });
  }

  componentWillUnmount() {
    ReportsStore.setApps([]);
  }

  /**
   * 加载数据
   */
  loadData = () => {
    const projectId = AppState.currentMenuType.id;
    const { appId } = this.state;
    this.setState({ loading: true });
    axios.post(`/devops/v1/projects/${projectId}/apps/${appId}/git/branches`)
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setState({ branchList: data.content });
        }
        this.setState({ loading: false });
      });
  };

  handleChange = (id) => {
    DevPipelineStore.setRecentApp(id);
    this.setState({ appId: id }, () => this.loadData());
  };

  getContent = () => {
    const { loading } = this.state;
    let list = [];
    if (loading) {
      return (<Spin className="c7n-dashboard-loading-position" />);
    }
    const { branchList } = this.state;
    if (branchList && branchList.length) {
      list = branchList.map((item) => {
        const { branchName, commitUserName, commitDate, commitUserUrl, commitUrl } = item;
        return (
          <div className="c7n-dashboard-branch-item" key={branchName}>
            {commitUserUrl
              ? <Avatar size="small" src={commitUserUrl} />
              : <Avatar size="small">{commitUserName ? commitUserName.toString().slice(0, 1).toUpperCase() : '?'}</Avatar>}
            <div className="c7n-report-history-info">
              <div className="c7n-report-history-content">
                <span>{branchName}</span>
              </div>
              <div className="c7n-report-history-date">
                <span>{commitUserName} </span>
                <FormattedMessage id="dashboard.commit.by" /> <TimePopover style={{ display: 'inline-block' }} content={commitDate} />
              </div>
            </div>
            <Tooltip
              title={<FormattedMessage id="dashboard.branch.request" />}
            >
              <a
                href={commitUrl && `${commitUrl.split('/commit')[0]}/merge_requests/new?change_branches=true&merge_request[source_branch]=${branchName}&merge_request[target_branch]=master`}
                target="_blank"
                rel="nofollow me noopener noreferrer"
              >
                <Button size="small" shape="circle" icon="merge_request" />
              </a>
            </Tooltip>
          </div>
        );
      });
    } else {
      list = (<div className="c7n-db-noData"><FormattedMessage id="dashboard.nobranch" /></div>);
    }
    return list;
  };

  render() {
    const { intl: { formatMessage } } = this.props;
    const { id: projectId, name: projectName, organizationId, type } = AppState.currentMenuType;
    const { getAllApps } = ReportsStore;
    const { appId, noSelect } = this.state;
    return (<Fragment>
      <Select
        className={`c7ncd-dashboard-select ${noSelect ? 'c7n-dashboard-build-select' : ''}`}
        notFoundContent={formatMessage({ id: 'dashboard.noApp' })}
        placeholder={formatMessage({ id: 'dashboard.environment.select' })}
        onChange={this.handleChange}
        defaultValue={appId}
        value={appId}
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
      <div className="c7ncd-db-panel pdb1">{this.getContent()}</div>
      <DashBoardNavBar>
        <Link
          to={{
            pathname: '/devops/branch',
            search: `?type=${type}&id=${projectId}&name=${encodeURIComponent(projectName)}&organizationId=${organizationId}`,
            state: { appId },
          }}
        >
          <FormattedMessage id="dashboard.branch" />
        </Link>
      </DashBoardNavBar>
    </Fragment>);
  }
}

export default withRouter(injectIntl(DevOpsBranch));
