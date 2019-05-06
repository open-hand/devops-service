import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { withRouter, Link } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content, Header, Page, Permission, stores } from '@choerodon/boot';
import { Button, Select, Modal, Form, Icon, Collapse, Avatar, Pagination, Tooltip, Menu, Dropdown, Progress } from 'choerodon-ui';
import ReactMarkdown from 'react-markdown';
import _ from 'lodash';
import moment from 'moment';
import classNames from "classnames";
import { CopyToClipboard } from 'react-copy-to-clipboard';
import LoadingBar from '../../../../components/loadingBar';
import TimePopover from '../../../../components/timePopover/index';
import AppTagCreate from '../../appTag/appTagCreate';
import AppTagEdit from '../../appTag/appTagEdit';
import '../../../main.scss';
import '../../appTag/appTagHome/AppTag.scss';
import './DevConsole.scss';
import '../../envPipeline/EnvPipeLineHome.scss';
import DevPipelineStore from '../../../../stores/project/devPipeline';
import AppTagStore from '../../../../stores/project/appTag';
import BranchStore from '../../../../stores/project/branchManage';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';
import BranchEdit from '../../branch/branchEdit';
import BranchCreate from '../../branch/branchCreate';
import IssueDetail from '../../branch/issueDetail';
import MergeRequestStore from '../../../../stores/project/mergeRequest';
import StatusTags from '../../../../components/StatusTags';
import ReportsStore from '../../../../stores/project/reports/ReportsStore';
import CiPipelineStore from '../../../../stores/project/ciPipelineManage/CiPipelineStore';
import AppVersionStore from '../../../../stores/project/applicationVersion/AppVersionStore';
import DepPipelineEmpty from '../../../../components/DepPipelineEmpty/DepPipelineEmpty';
import CiPipelineTable from '../../ciPipelineManage/ciPipelineTable';
import AppVersionTable from '../../appVersion/appVersionTable';
import Percentage from "../../../../components/percentage/Percentage";
import Rating from "../../../../components/rating/Rating";

const { AppState } = stores;
const { Option, OptGroup } = Select;
const { Panel } = Collapse;
const START = moment().subtract(7, 'days').format().split('T')[0].replace(/-/g, '/');
const END = moment().format().split('T')[0].replace(/-/g, '/');
const ICONS = {
  passed: {
    icon: 'icon-check_circle',
    code: 'passed',
    display: 'Passed',
  },
  success: {
    icon: 'icon-check_circle',
    code: 'success',
    display: 'Passed',
  },
  pending: {
    icon: 'icon-pause_circle_outline',
    code: 'pending',
    display: 'Pending',
  },
  running: {
    icon: 'icon-timelapse',
    code: 'running',
    display: 'Running',
  },
  failed: {
    icon: 'icon-cancel',
    code: 'failed',
    display: 'Failed',
  },
  canceled: {
    icon: 'icon-cancle_b',
    code: 'canceled',
    display: 'Canceled',
  },
  skipped: {
    icon: 'icon-skipped_b',
    code: 'skipped',
    display: 'Skipped',
  },
  created: {
    icon: 'icon-radio_button_checked',
    code: 'created',
    display: 'Created',
  },
  manual: {
    icon: 'icon-radio_button_checked',
    code: 'manual',
    display: 'Manual',
  },
};

@observer
class DevConsole extends Component {
  constructor(props) {
    super(props);
    this.state = {
      page: 0,
      pageSize: 10,
      deleteLoading: false,
      tagName: null,
      tagRelease: null,
      loadingFlag: false,
      versionState: false,
      appName: null,
      branchIssue: 'ALL',
      lineKey: 1,
      modalDisplay: '',
    };
  }

  componentDidMount() {
    AppTagStore.setLoading(null);
    AppTagStore.setTagData([]);
    this.loadInitData();
  }

  componentWillUnmount() {
    const { DevConsoleStore } = this.props;
    DevConsoleStore.setBranchList([]);
    ReportsStore.setCommits({});
    MergeRequestStore.setMerge([], 'opened');
    MergeRequestStore.setMerge([], 'merged');
  }


  /**
   * 通过下拉选择器选择应用时，获取应用id
   * @param id
   * @param option
   */
  handleSelect = (id, option) => {
    this.setState({ page: 0, pageSize: 10, appName: option.props.children });
    DevPipelineStore.setSelectApp(id);
    DevPipelineStore.setRecentApp(id);
    this.setState({
      branchIssue: 'ALL',
    }, () => {
      this.handleRefresh();
    });
  };

  /**
   * 页面内刷新，选择器变回默认选项
   */
  handleRefresh = () => {
    const { page, pageSize, branchIssue } = this.state;
    this.setState({ loadingFlag: true });
    const appId = DevPipelineStore.getSelectApp;
    const projectId = AppState.currentMenuType.id;
    this.loadTagData(page, pageSize);
    this.loadBranchData();
    this.loadMergeData();
    this.loadCommitData();
    this.loadPipeline(branchIssue);
    AppVersionStore.loadData(projectId, appId);
    DevPipelineStore.queryAppData(projectId);
  };

  /**
   * 加载数据
   */
  loadInitData = () => {
    DevPipelineStore.queryAppData(AppState.currentMenuType.id, 'all');
    this.loadPipeline(this.state.branchIssue);
    this.setState({ appName: null });
  };

  /**
   * 加载分支信息
   */
  loadBranchData = () => {
    const { DevConsoleStore } = this.props;
    const { projectId } = AppState.currentMenuType;
    DevConsoleStore.loadBranchList(projectId, DevPipelineStore.getSelectApp);
  };

  /**
   * 加载合并请求信息
   */
  loadMergeData = () => {
    const { projectId } = AppState.currentMenuType;
    MergeRequestStore.loadMergeRquest(DevPipelineStore.getSelectApp, 'opened', 0, 5);
    MergeRequestStore.loadMergeRquest(DevPipelineStore.getSelectApp, 'merged', 0, 5);
    MergeRequestStore.loadUrl(projectId, DevPipelineStore.getSelectApp);
  };

  /**
   * 加载提交记录
   */
  loadCommitData = () => {
    const { projectId } = AppState.currentMenuType;
    ReportsStore.loadCommits(projectId, START, END, [DevPipelineStore.getSelectApp]);
  };

  /**
   * 加载刷新tag列表信息
   * @param page
   * @param pageSize
   */
  loadTagData = (page = 0, pageSize = 10) => {
    const { projectId } = AppState.currentMenuType;
    AppTagStore.queryTagData(projectId, page, pageSize);
  };

  loadPipeline = (branch) => {
    const appId = DevPipelineStore.getSelectApp;
    this.setState({ loadingFlag: false });
    if (branch !== 'ALL') {
      CiPipelineStore.loadPipelinesByBc(appId, branch)
        .then((res) => {
          const ciPipelineOne = res.length ? res[0] : null;
          this.setState({ versionState: ciPipelineOne ? ciPipelineOne.version : false })
        });
    } else {
      if (appId) {
        CiPipelineStore.loadPipelines(true, appId)
          .then((res) => {
            const ciPipelineOne = res.length ? res[0] : null;
            this.setState({ versionState: ciPipelineOne ? ciPipelineOne.version : false });
          });
      }
    }
  };

  /**
   * 获取CI pipeline
   */
  loadPipelineThrottle = _.throttle((branch) => this.loadPipeline(branch), 20000);

  /**
   * 分页器
   * @param current
   * @param size
   */
  handlePaginChange = (current, size) => {
    this.setState({ page: current - 1, pageSize: size });
    this.loadTagData(current - 1, size);
  };

  /**
   * 删除tag
   */
  deleteTag = () => {
    const { projectId } = AppState.currentMenuType;
    const { tagName } = this.state;
    this.setState({ deleteLoading: true });
    AppTagStore.deleteTag(projectId, tagName).then((data) => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.loadTagData();
      }
      this.setState({ deleteLoading: false, modalDisplay: 'close' });
    }).catch((error) => {
      this.setState({ deleteLoading: false });
      Choerodon.handleResponseError(error);
    });
  };

  /**
   * 控制创建窗口显隐
   * @param flag
   * @param name 名称
   * @param tagRelease release内容
   */
  displayModal = (flag, name, tagRelease) => {
    const { projectId } = AppState.currentMenuType;
    switch ( flag ) {
      case 'editTag':
        this.setState({ tagName: name, tagRelease });
        break;
      case 'deleteTag':
        this.setState({ tagName: name });
        break;
      case 'createBranch':
        BranchStore.loadTagData(projectId);
        BranchStore.loadBranchData({
          projectId,
          size: 3,
        });
        BranchStore.setCreateBranchShow('create');
        break;
      case 'editBranch':
        this.setState({ branchName: name });
        BranchStore.loadBranchByName(projectId, DevPipelineStore.selectedApp, name);
        BranchStore.setCreateBranchShow('edit');
        break;
      case 'deleteBranch':
        this.setState({ branchName: name });
        break;
      case 'closeBranch':
        BranchStore.setCreateBranchShow(false);
        BranchStore.setBranch(null);
        break;
      default:
        break;
    }
    this.setState({ modalDisplay: flag });
  };

  /**
   * 点击复制代码成功回调
   * @returns {*|string}
   */
  handleCopy = () => Choerodon.prompt('复制成功');

  /**
   * 获取列表的icon
   * @param name 分支名称
   * @returns {*}
   */
  getIcon =(name) => {
    const nameArr = ['feature', 'release', 'bugfix', 'hotfix'];
    let type = '';
    if (name.includes('-') && nameArr.includes(name.split('-')[0])) {
      type = name.split('-')[0];
    } else if (name === 'master') {
      type = name;
    } else {
      type = 'custom';
    }
    return <span className={`c7n-branch-icon icon-${type}`}>{type.slice(0, 1).toUpperCase()}</span>;
  };

  /**
   * 删除分支
   */
  deleteBranch = () => {
    const { branchName } = this.state;
    const { projectId } = AppState.currentMenuType;
    this.setState({ deleteLoading: true });
    BranchStore.deleteData(projectId, DevPipelineStore.getSelectApp, branchName)
      .then((res) => {
        if (res) {
          this.loadBranchData();
          this.setState({ deleteLoading: false, modalDisplay: 'close' });
        }
        this.setState({ deleteLoading: false });
      });
  };

  /**
   * 切换分支
   * @param branchIssue
   */
  branchChange = (branchIssue) => {
    const appId = DevPipelineStore.getSelectApp;
    const { projectId } = AppState.currentMenuType;
    this.setState({ branchIssue });
    if (branchIssue !== 'ALL') {
      AppVersionStore.loadVerByBc(projectId, appId, branchIssue);
    } else {
      AppVersionStore.loadData(projectId, appId);
    }
    this.loadPipeline(branchIssue);
  };

  /**
   * 流水线卡片点击加载
   * @param key
   */
  cardChecked = (key) => {
    if (this.state.lineKey === key) {
      return;
    }
    this.setState({ lineKey: key, loadingFlag: false });
    const { branchIssue } = this.state;
    const appId = DevPipelineStore.getSelectApp;
    const projectId = AppState.currentMenuType.id;
    this.loadBranchData();
    this.loadMergeData();
    this.loadCommitData();
    if (branchIssue !== 'ALL') {
      this.loadPipeline(branchIssue);
      AppVersionStore.loadVerByBc(projectId, appId, branchIssue);
    } else {
      this.loadPipeline(branchIssue);
      AppVersionStore.loadData(projectId, appId);
    }
  };

  /**
   * 获取issue的options
   * @param s
   * @param type
   * @returns {*}
   */
  getOptionContent = (s, type) => {
    const { formatMessage } = this.props.intl;
    let mes = '';
    let icon = '';
    let color = '';
    switch (s.typeCode) {
      case 'story':
        mes = formatMessage({ id: 'branch.issue.story' });
        icon = 'agile_story';
        color = '#00bfa5';
        break;
      case 'bug':
        mes = formatMessage({ id: 'branch.issue.bug' });
        icon = 'agile_fault';
        color = '#f44336';
        break;
      case 'issue_epic':
        mes = formatMessage({ id: 'branch.issue.epic' });
        icon = 'agile_epic';
        color = '#743be7';
        break;
      case 'sub_task':
        mes = formatMessage({ id: 'branch.issue.subtask' });
        icon = 'agile_subtask';
        color = '#4d90fe';
        break;
      default:
        mes = formatMessage({ id: 'branch.issue.task' });
        icon = 'agile_task';
        color = '#4d90fe';
    }
    return (<Fragment>
        <Tooltip title={mes}>
          <div style={{ color }} className={`c7n-dc-branch-issue-icon-${type}`}><i className={`icon icon-${icon}`} /></div>
        </Tooltip>
        <a onClick={this.showIssue.bind(this, s.issueId, s.branchName)} role="none">
          <Tooltip title={s.issueName}>{s.issueCode}</Tooltip>
        </a>
    </Fragment>);
  };

  /**
   * 加载分支关联问题
   * @param id
   * @param name
   */
  showIssue = (id, name) => {
    const { projectId } = AppState.currentMenuType;
    this.setState({ branchName: name });
    BranchStore.loadIssueById(projectId, id);
    BranchStore.loadIssueTimeById(projectId, id);
    BranchStore.setCreateBranchShow('detail');
  };

  /**
   * 渲染ci阶段
   * @param record
   * @returns {*}
   */
  renderStages = (record) => {
    const pipeStage = [];
    let stages = record ? record.stages : null;
    if (stages && stages.length) {
      if (stages.length > 3) {
        stages = record.stages.slice(record.stages.length - 3, record.stages.length);
      }
      for (let i = 0, l = stages.length; i < l; i += 1) {
        pipeStage.push(<span className="c7n-jobs" key={i}>
          {i !== 0 ? <span className="c7n-split-before" /> : null}
          <Tooltip
            title={(stages[i].name === 'sonarqube' && stages[i].status === 'failed') ? `${stages[i].name} : ${stages[i].description}` : `${stages[i].name} : ${stages[i].status}`}
          >
            {stages[i].name === 'sonarqube' ?
              <i
                className={`icon ${ICONS[stages[i].status || 'skipped'].icon || ''} c7n-icon-${stages[i].status}`}
              /> :
              <a
                className="" href={record.gitlabUrl ? `${record.gitlabUrl.slice(0, -4)}/-/jobs/${stages[i].id}` : null}
                target="_blank"
                rel="nofollow me noopener noreferrer"
              >
                <i
                  className={`icon ${ICONS[stages[i].status || 'skipped'].icon || ''}
                c7n-icon-${stages[i].status}`}
                />
              </a>}
          </Tooltip>
        </span>);
      }
    } else {
      pipeStage.push(<span className="c7n-jobs" key="c7n-stage-null">
        <Icon type="wait_circle" />
        <span className="c7n-split-before" />
        <Icon type="wait_circle" />
        <span className="c7n-split-before" />
        <Icon type="wait_circle" />
      </span>);
    }
    return (
      <div className="c7n-jobs">
        {pipeStage}
      </div>
    );
  };

  /**
   * 第二阶段卡片内容
   * @param data
   * @returns {*}
   */
  getCardContent(data) {
    this.loadPipelineThrottle(this.state.branchIssue);
    const { intl: { formatMessage } } = this.props;
    let styles = "";
    let status = null;
    if (!data) {
      styles = "c7n-env-state-unexecuted";
    } else {
      status = data.status;
    }
    if (status === 'failed') {
      styles = "c7n-env-state-failed";
    } else if (status === 'passed') {
      styles = "c7n-env-state-running";
    } else if (status === 'running') {
      styles = "c7n-env-state-creating";
    } else if (status === 'pending' || status === 'created') {
      styles = "c7n-env-state-disconnect";
    } else {
      styles = "c7n-env-state-unexecuted";
    }
    return (
      <div className="c7n-env-card-content">
        <div className={classNames("c7n-env-state", styles)}>
          {formatMessage({ id: `ci_${status || 'unexecuted'}` })}
        </div>
        <div className="c7n-env-des-wrap">
          <div className="c7n-env-des">
            <div className="c7n-dc-ci">
              {formatMessage({ id: 'app.stage' })}：
              {this.renderStages(data)}
            </div>
          </div>
        </div>
      </div>
    );
  }

  /**
   * 跳转至构建次数报表
   */
  linkToBuild = () => {
    const { history } = this.props;
    const { type, projectId, organizationId: orgId, name } = AppState.currentMenuType;
    history.push({
      pathname: "/devops/reports/build-number",
      search: `?type=${type}&id=${projectId}&name=${name}&organizationId=${orgId}`,
      state: {
        appId: DevPipelineStore.getSelectApp,
        backPath: "dev-console",
      },
    })
  };

  /**
   * 获取流水线
   * @returns {*}
   */
  getDevPipeline = () => {
    const { type, projectId, organizationId: orgId } = AppState.currentMenuType;
    const { lineKey, branchIssue, loadingFlag, versionState } = this.state;
    const { DevConsoleStore, intl: { formatMessage } } = this.props;
    const branchLoading = DevConsoleStore.getBranchLoading;
    const { loading: mergeLoading } = MergeRequestStore;
    const branchList = DevConsoleStore.getBranchList;
    const ciPipelines = CiPipelineStore.getCiPipelines;
    const ciPipelineOne = ciPipelines.length ? ciPipelines[0] : null;
    const branchOption = _.map(branchList, b => <Option key={b.sha} value={b.branchName}>{b.branchName}</Option>);
    const branchOne = _.filter(branchList, ['branchName', branchIssue]).length ? _.filter(branchList, ['branchName', branchIssue])[0] : {branchName: <FormattedMessage id="devCs.allBranch" />};
    const issueDom = branchOne && (<div className='c7n-dc-branch-issue'>{branchOne.typeCode ?
      this.getOptionContent(branchOne, 'line') : null}</div>);
    const verContent = versionState ? (<div className="c7n-env-card-content">
      <div className="c7n-env-state c7n-env-state-running">
        {formatMessage({ id: 'ci_passed' })}
      </div>
      <div className="c7n-dc-version-span">
        <span>{formatMessage({ id: 'devCs.ver' })}</span>
      </div>
    </div>) : (<div className="c7n-dc-version-content">
      <div className="c7n-dc-noVersion"/>
      <span>{formatMessage({ id: 'report.build-duration.noversion' })}</span>
    </div>);

    return (<div className="c7n-dc-card-wrap">
        <div className="c7n-dc-card-title">
          <Icon type="line" />
          <FormattedMessage id="app.pipeline" />
        </div>
        <div className="c7n-dc-line">
          <div className={lineKey === 1 ? 'c7n-dc-line_card-active' : 'c7n-dc-line_card'} onClick={this.cardChecked.bind(this, 1)}>
            <div className="c7n-dc-line_card-arrow" />
            <div className="c7n-dc-line_card-content">
              <div className="c7n-dc-line_card-title">
                <FormattedMessage id="app.branchManage" />
                {branchList && branchList.length ? <Permission
                  service={['devops-service.devops-git.createBranch']}
                  type={type}
                  projectId={projectId}
                  organizationId={orgId}
                >
                  <Tooltip
                    placement="bottom"
                    title={<FormattedMessage id="branch.create" />}
                  >
                    <Button
                      icon="add_branch"
                      shape="circle"
                      onClick={this.displayModal.bind(this, 'createBranch')}
                    />
                  </Tooltip>
                </Permission> : null}
              </div>
              <Select
                label={<FormattedMessage id="app.branch.select" />}
                onChange={this.branchChange}
                value={branchOne ? branchOne.branchName : null}
              >
                {branchOption}
                <Option key={'ALL'}><FormattedMessage id="devCs.allBranch" /></Option>
              </Select>
            </div>
          </div>
          <div className="c7n-dc-line-arrow" >
            {issueDom}
          </div>
          <div className={lineKey === 2 ? 'c7n-dc-line_card-active' : 'c7n-dc-line_card'} onClick={this.cardChecked.bind(this, 2)}>
            <div className="c7n-dc-line_card-arrow" />
            <div className="c7n-dc-line_card-content">
              <div className="c7n-dc-line_card-title">
                <FormattedMessage id="ciPipeline.head" />
                <Tooltip
                  placement="bottom"
                  title={<FormattedMessage id="devCs.build" />}
                >
                  <Button
                    icon="poll"
                    shape="circle"
                    onClick={this.linkToBuild}
                  />
                </Tooltip>
              </div>
              {CiPipelineStore.loading && loadingFlag ? <Progress className="c7n-dc-card-loading" type="loading" /> : this.getCardContent(ciPipelineOne)}
            </div>
          </div>
          <div className="c7n-dc-line-arrow" />
          <div className={lineKey === 3 ? 'c7n-dc-line_card-active' : 'c7n-dc-line_card'} onClick={this.cardChecked.bind(this, 3)}>
            <div className="c7n-dc-line_card-arrow" />
            <div className="c7n-dc-line_card-content">
              <div className="c7n-dc-line_card-title">
                <FormattedMessage id="app.version" />
              </div>
              {CiPipelineStore.loading && loadingFlag ? <Progress className="c7n-dc-card-loading" type="loading" /> : verContent}
            </div>
          </div>
        </div>
      {lineKey === 1 ? <Fragment>{branchLoading && mergeLoading  && loadingFlag ? <LoadingBar display /> : (<Fragment>
        {this.getBranch()}
        {this.getMergeRequest()}
      </Fragment>)}
      </Fragment> : null}
      {lineKey === 2 ? <Fragment>{CiPipelineStore.loading  && loadingFlag ? <LoadingBar display /> : (<div className="c7n-dc-branch c7n-ciPipeline">
          <div className="c7n-dc-card-subtitle">
            <FormattedMessage id="ciPipeline.head" />
          </div>
          <CiPipelineTable store={CiPipelineStore} loading={CiPipelineStore.loading  && loadingFlag}/>
        </div>)}
      </Fragment> : null}
      {lineKey === 3 ? <Fragment>{AppVersionStore.loading  && loadingFlag ? <LoadingBar display /> : (<div className="c7n-dc-branch c7n-ciPipeline">
          <div className="c7n-dc-card-subtitle">
            <FormattedMessage id="app.version" />
          </div>
          <AppVersionTable store={AppVersionStore} loading={AppVersionStore.loading  && loadingFlag} />
        </div>)}
      </Fragment> : null}
    </div>)};

  /**
   * 获取分支内容
   */
  getBranch = () => {
    const { DevConsoleStore } = this.props;
    const branchList = DevConsoleStore.getBranchList;
    let list = [];
    if (branchList && branchList.length) {
      list = branchList.map((item) => {
        const { branchName, commitUserName, commitDate, commitUserUrl, commitUrl, typeCode, sha, commitContent } = item;
        const { type, projectId, organizationId: orgId } = AppState.currentMenuType;
        return (<div className="c7n-dc-branch-content" key={branchName}>
          <div className="branch-content-title">
            {this.getIcon(branchName)}
            <MouserOverWrapper text={branchName} width={0.2}>{branchName}</MouserOverWrapper>
          </div>
          <div className="c7n-branch-commit">
            <i className="icon icon-point branch-column-icon" />
            <a href={commitUrl} target="_blank" rel="nofollow me noopener noreferrer" className="branch-sha">
              <span>{sha && sha.slice(0, 8) }</span>
            </a>
            <i className="icon icon-schedule branch-col-icon branch-column-icon" />
            <div className="c7n-branch-time"><TimePopover content={commitDate} /></div>
            <Tooltip title={commitUserName}>
              {commitUserUrl
                ? <Avatar size="small" src={commitUserUrl} className="c7n-branch-avatar" />
                : <Avatar size="small" className="c7n-branch-avatar">{commitUserName ? commitUserName.toString().slice(0, 1).toUpperCase() : '?'}</Avatar>
              }
            </Tooltip>
            <MouserOverWrapper text={commitContent} width={0.3}>{commitContent}</MouserOverWrapper>
          </div>
          <div className="c7n-branch-operate">
            {typeCode ? this.getOptionContent(item, 'list') : null}
            {branchName !== 'master' ? <div className="c7n-branch-action">
              <Permission
                projectId={projectId}
                organizationId={orgId}
                type={type}
                service={['devops-service.devops-git.update']}
              >
                <Tooltip title={<FormattedMessage id="branch.edit" />}>
                  <Button size="small" shape="circle" icon="mode_edit" onClick={this.displayModal.bind(this, 'editBranch', branchName)} />
                </Tooltip>
              </Permission>
              <Tooltip title={<FormattedMessage id="branch.request" />}>
                <a
                  href={commitUrl && `${commitUrl.split('/commit')[0]}/merge_requests/new?change_branches=true&merge_request[source_branch]=${branchName}&merge_request[target_branch]=master`}
                  target="_blank"
                  rel="nofollow me noopener noreferrer"
                >
                  <Button size="small" shape="circle" icon="merge_request" />
                </a>
              </Tooltip>
              <Permission
                projectId={projectId}
                organizationId={orgId}
                type={type}
                service={['devops-service.devops-git.delete']}
              >
                <Tooltip title={<FormattedMessage id="delete" />}>
                  <Button size="small" shape="circle" icon="delete_forever" onClick={this.displayModal.bind(this, 'deleteBranch', branchName)} />
                </Tooltip>
              </Permission>
            </div> : null}
          </div>
        </div>);
      });
    }
    return (<div className="c7n-dc-branch">
      <div className="c7n-dc-card-subtitle">
        <FormattedMessage id="app.branch" />
      </div>
      {list}
      </div>);
  };

  /**
   * 查看合并请求详情
   */
  linkToMerge = (iid) => {
    let url = '';
    if (iid) {
      url = `${MergeRequestStore.getUrl}/merge_requests/${iid}`;
    } else {
      url = `${MergeRequestStore.getUrl}/merge_requests/new`;
    }
    window.open(url);
  };

  /**
   * 获取合并请求内容
   */
  getMergeRequest = () => {
    const appData = DevPipelineStore.getAppData;
    const { opened, merged } = MergeRequestStore.getMerge;
    const mergeList = opened.concat(merged).slice(0, 5);
    let list = [];
    if (mergeList && mergeList.length) {
      list = mergeList.map((item) => {
        const { iid, sourceBranch, targetBranch, title, state, author, updatedAt } = item;
        const { type, projectId, organizationId: orgId } = AppState.currentMenuType;
        return (<div className="c7n-dc-branch-content" key={iid}>
          <div className="branch-content-title">
            <StatusTags name={state} colorCode={state} />
            <span className="c7n-merge-title">!{iid}</span>
            <MouserOverWrapper text={title} width={0.3}>{title}</MouserOverWrapper>
          </div>
          <div className="c7n-merge-branch">
            <Icon type="branch" className="c7n-merge-icon" />
            <MouserOverWrapper text={sourceBranch} width={0.15}>{sourceBranch}</MouserOverWrapper>
            <span><Icon type="keyboard_backspace" className="c7n-merge-icon-arrow" /></span>
            <Icon type="branch" className="c7n-merge-icon" />
            <MouserOverWrapper text={targetBranch} width={0.15}>{targetBranch}</MouserOverWrapper>
          </div>
          <div className="c7n-branch-operate">
            <div className="c7n-merge-branch">
              <Tooltip title={author ? author.name : null}>
                {author && author.webUrl
                  ? <Avatar size="small" src={author.webUrl} className="c7n-branch-avatar" />
                  : <Avatar size="small" className="c7n-branch-avatar">{author ? author.name.toString().slice(0, 1).toUpperCase() : '?'}</Avatar>
                }
              </Tooltip>
              <span>{author ? author.name : 'unknown'}</span>
              <i className="icon icon-schedule issue-time" />
              <div className="issue-time_content">
                <TimePopover content={updatedAt} />
              </div>
            </div>
            <Permission
              service={['devops-service.devops-git.getMergeRequestList']}
              organizationId={orgId}
              projectId={projectId}
              type={type}
            >
              <Tooltip title={<FormattedMessage id="merge.detail" />}>
                <Button
                  size="small"
                  shape="circle"
                  icon="find_in_page"
                  onClick={this.linkToMerge.bind(this, iid)}
                />
              </Tooltip>
            </Permission>
          </div>
        </div>);
      });
    } else if (appData && appData.length) {
      list = (<div className="c7n-devCs-nomerge"><FormattedMessage id="devCs.nomerge" /></div>);
    }
    return (<div className="c7n-dc-branch">
      <div className="c7n-dc-card-subtitle">
        <FormattedMessage id="merge.head" />
      </div>
      {list}
    </div>);
  };

  render() {
    const { intl: { formatMessage } } = this.props;
    const { type, projectId, organizationId: orgId, name } = AppState.currentMenuType;
    const { modalDisplay, deleteLoading, appName, tagRelease, tagName, branchName } = this.state;
    const appData = DevPipelineStore.getAppData;
    const appId = DevPipelineStore.getSelectApp;
    const tagData = AppTagStore.getTagData;
    const loading = AppTagStore.getLoading;
    const currentAppName = appName || DevPipelineStore.getDefaultAppName;
    const { current, total, pageSize } = AppTagStore.pageInfo;
    const tagList = [];
    const { DevConsoleStore } = this.props;
    const branchList = DevConsoleStore.getBranchList;
    const { totalCommitsDate } = ReportsStore.getCommits;
    const { getCount: { totalCount } } = MergeRequestStore;

    const titleName = _.find(appData, ['id', appId]) ? _.find(appData, ['id', appId]).name : name;
    const currentApp = _.find(appData, ['id', appId]);

    const numberData = [
      {
        name: 'branch',
        number: branchList.length,
        message: 'devCs.branch.number',
        icon: 'branch',
      },
      {
        name: 'merge-request',
        number: totalCount,
        message: 'devCs.merge.number',
        icon: 'merge_request',
      },
      {
        name: 'tag',
        number: total,
        message: 'devCs.tag.number',
        icon: 'local_offer',
      },
    ];

    const menu = (
      <Menu className="c7n-envow-dropdown-link">
        <Menu.Item
          key="0"
        >
          <Permission
            service={['devops-service.devops-git.createBranch']}
            type={type}
            projectId={projectId}
            organizationId={orgId}
          >
            <Button
              funcType="flat"
              onClick={this.displayModal.bind(this, 'createBranch')}
            >
              <FormattedMessage id="branch.create" />
            </Button>
          </Permission>
        </Menu.Item>
        <Menu.Item
          key="1"
        >
          <Permission
            service={[
              'devops-service.devops-git.createTag',
            ]}
            type={type}
            projectId={projectId}
            organizationId={orgId}
          >
            <Button
              funcType="flat"
              onClick={this.displayModal.bind(this, 'createTag')}
            >
              <FormattedMessage id="apptag.create" />
            </Button>
          </Permission>
        </Menu.Item>
        <Menu.Item
          key="2"
        >
          <Button
            funcType="flat"
            onClick={this.linkToMerge.bind(this, false)}
          >
            <FormattedMessage id="merge.createMerge" />
          </Button>
        </Menu.Item>
      </Menu>
    );

    _.forEach(tagData, (item) => {
      const {
        commit: {
          authorName,
          committedDate,
          message: commitMsg,
          shortId,
          url,
        },
        commitUserImage,
        tagName,
        release,
      } = item;
      const header = (<div className="c7n-tag-panel">
        <div className="c7n-tag-panel-info">
          <div className="c7n-tag-panel-name">
            <Icon type="local_offer" />
            <span>{tagName}</span>
          </div>
          <div className="c7n-tag-panel-detail">
            <Icon className="c7n-tag-icon-point" type="point" />
            <a href={url} rel="nofollow me noopener noreferrer" target="_blank">{shortId}</a>
            <span className="c7n-divide-point">&bull;</span>
            <span className="c7n-tag-msg">{commitMsg}</span>
            <span className="c7n-divide-point">&bull;</span>
            <span className="c7n-tag-panel-person">
              {commitUserImage
                ? <Avatar className="c7n-tag-commit-img" src={commitUserImage} />
                : <span className="c7n-tag-commit c7n-tag-commit-avatar">{authorName.toString().substr(0, 1)}</span>}
              <span className="c7n-tag-commit">{authorName}</span>
            </span>
            <span className="c7n-divide-point">&bull;</span>
            <div className="c7n-tag-time"><TimePopover content={committedDate} /></div>
          </div>
        </div>
        <div className="c7n-tag-panel-opera">
          <Permission
            service={[
              'devops-service.devops-git.updateTagRelease',
            ]}
            type={type}
            projectId={projectId}
            organizationId={orgId}
          >
            <Tooltip
              placement="bottom"
              title={<FormattedMessage id="edit" />}
            >
              <Button
                shape="circle"
                size="small"
                icon="mode_edit"
                onClick={this.displayModal.bind(this, 'editTag', tagName, release)}
              />
            </Tooltip>
          </Permission>
          <Permission
            type={type}
            projectId={projectId}
            organizationId={orgId}
            service={[
              'devops-service.devops-git.deleteTag',
            ]}
          >
            <Tooltip
              placement="bottom"
              title={<FormattedMessage id="delete" />}
            >
              <Button
                shape="circle"
                size="small"
                icon="delete_forever"
                onClick={this.displayModal.bind(this, 'deleteTag', tagName)}
              />
            </Tooltip>
          </Permission>
        </div>
      </div>);
      tagList.push(<Panel
        header={header}
        key={tagName}
      >
        <div className="c7n-tag-release">{release ? <div className="c7n-md-parse">
          <ReactMarkdown
            source={release.description !== 'empty' ? release.description : formatMessage({ id: 'apptag.release.empty' })}
            skipHtml={false}
            escapeHtml={false}
          />
        </div> : formatMessage({ id: 'apptag.release.empty' })}</div>
      </Panel>);
    });
    const empty = appData && appData.length ? 'tag' : 'app';
    const noRepoUrl = formatMessage({ id: 'repository.noUrl' });

    const qualityData = [
      {
        icon: "fiber_smart_record",
        title: formatMessage({ id: "codeQuality.coverage" }),
        number: 70,
        rating: null,
      },
      {
        icon: "bug_report",
        title: "Bugs",
        number: 12,
        rating: "A",
      },
      {
        icon: "unlock",
        title: formatMessage({ id: "codeQuality.vulnerabilities" }),
        number: 4,
        rating: "B",
      },
      {
        icon: "opacity",
        title: formatMessage({ id: "codeQuality.debt" }),
        number: "10天",
        rating: "C",
      },
      {
        icon: "group_work",
        title: formatMessage({ id: "codeQuality.code.smells" }),
        number: 125,
        rating: "D",
      },
      {
        icon: null,
        title: "Java,XML",
        number: 552,
        rating: "S",
      },
    ];

    return (
      <Page
        className="c7n-tag-wrapper"
        service={[
          'devops-service.application.listByActive',
          'devops-service.devops-git.getTagByPage',
          'devops-service.devops-git.listByAppId',
          'devops-service.devops-git.updateTagRelease',
          'devops-service.devops-git.createTag',
          'devops-service.devops-git.checkTag',
          'devops-service.devops-git.deleteTag',
          'devops-service.devops-git.listByAppId',
          'devops-service.devops-git.createBranch',
          'devops-service.devops-git.update',
          'devops-service.devops-git.delete',
          'devops-service.devops-git.getMergeRequestList',
        ]}
      >
        {appData && appData.length && appId ? <Fragment><Header title={<FormattedMessage id="devCs.head" />}>
          <Select
            filter
            className="c7n-header-select"
            dropdownClassName="c7n-header-select_drop"
            placeholder={formatMessage({ id: 'ist.noApp' })}
            value={appData && appData.length ? DevPipelineStore.getSelectApp : undefined}
            disabled={appData.length === 0}
            filterOption={(input, option) => option.props.children.props.children.props.children
              .toLowerCase().indexOf(input.toLowerCase()) >= 0}
            onChange={(value, option) => this.handleSelect(value, option)}
          >
            <OptGroup label={formatMessage({ id: 'recent' })} key="recent">
              {
                _.map(DevPipelineStore.getRecentApp, app => (
                  <Option
                    key={`recent-${app.id}`}
                    value={app.id}
                    title={app.name}
                    disabled={!app.permission}
                  >
                    <Tooltip title={app.code}><span className="c7n-ib-width_100">{app.name}</span></Tooltip>
                  </Option>))
              }
            </OptGroup>
            <OptGroup label={formatMessage({ id: 'deploy.app' })} key="app">
              {
                _.map(appData, (app, index) => (
                  <Option
                    value={app.id}
                    key={index}
                    title={app.name}
                    disabled={!app.permission}
                  >
                    <Tooltip title={app.code}><span className="c7n-ib-width_100">{app.name}</span></Tooltip>
                  </Option>))
              }
            </OptGroup>
          </Select>
          {branchList && branchList.length ? (<div className="c7n-dc-create-select">
            <Dropdown overlay={menu} trigger={['click']}>
              <a href="#">
                <Icon type="playlist_add" />
                {formatMessage({ id: 'create' })}
                <Icon type="arrow_drop_down" />
              </a>
            </Dropdown>
          </div>) : null}
          <Button
            type="primary"
            funcType="flat"
            icon="refresh"
            onClick={this.handleRefresh}
          >
            <FormattedMessage id="refresh" />
          </Button>
        </Header>
        <Content>
          {/*page-header*/}
          <div className="c7n-dc-page-content-header">
            <div className="page-content-header">
              <div className="title">{appData.length && appId ? `应用"${titleName}"的开发控制台` : `项目"${titleName}"的开发控制台`}</div>
              {appData && appData.length ? <Fragment>
                <div className="c7n-dc-app-code">
                  <FormattedMessage id="ciPipeline.appCode" />：{currentApp ? currentApp.code : ''}
                  {currentApp && currentApp.sonarUrl ? <Tooltip title={<FormattedMessage id="repository.quality" />} placement="bottom">
                    <a className="repo-copy-btn" href={currentApp.sonarUrl} rel="nofollow me noopener noreferrer" target="_blank">
                      <Button shape="circle" size="small" icon="quality" />
                    </a>
                  </Tooltip> : null }
                </div>
                <div className="c7n-dc-url-wrap">
                  <FormattedMessage id="app.url" />：
                  {currentApp && currentApp.repoUrl ? (<a href={currentApp.repoUrl || null} rel="nofollow me noopener noreferrer" target="_blank">
                    <Tooltip title={currentApp.repoUrl}>
                      {`../${currentApp.repoUrl.split('/')[currentApp.repoUrl.split('/').length - 1]}`}
                    </Tooltip>
                  </a>) : ''}
                  {currentApp && currentApp.repoUrl ? <Tooltip title={<FormattedMessage id="repository.copyUrl" />} placement="bottom">
                    <CopyToClipboard
                      text={currentApp.repoUrl || noRepoUrl}
                      onCopy={this.handleCopy}
                    >
                      <Button shape="circle" size="small">
                        <i className="icon icon-library_books" />
                      </Button>
                    </CopyToClipboard>
                  </Tooltip> : null}
                </div>
              </Fragment> : <div className="c7n-tag-empty">
                <Icon type="info" className="c7n-tag-empty-icon" />
                <span className="c7n-tag-empty-text">{formatMessage({ id: `apptag.${empty}.empty` })}</span>
              </div>}
            </div>
            <div className="c7n-dc-data-wrap">
              <div className="commit-number">
                <Link
                  to={{
                    pathname: `/devops/reports/submission`,
                    search: `?type=${type}&id=${projectId}&name=${encodeURIComponent(name)}&organizationId=${orgId}`,
                    state: {
                      backPath: `/devops/dev-console?type=${type}&id=${projectId}&name=${name}&organizationId=${orgId}`,
                      appId: appData && appData.length ? [DevPipelineStore.getSelectApp] : [] },
                  }}
                >
                  { totalCommitsDate ? totalCommitsDate.length : 0}
                </Link>
              </div>
              <div className="c7n-commit-title"><FormattedMessage id="devCs.commit.number" /></div>
            </div>
            <div className="c7n-dc-data-wrap">
              <table>
                {
                  _.map(numberData, item => (<tr className="c7n-data-number" key={item.name}>
                    <td className="c7n-data-number_link">
                      <Link
                        to={{
                          pathname: `/devops/${item.name}`,
                          search: `?type=${type}&id=${projectId}&name=${encodeURIComponent(name)}&organizationId=${orgId}`,
                          state: { backPath: `/devops/dev-console?type=${type}&id=${projectId}&name=${name}&organizationId=${orgId}` },
                        }}
                      >
                        {item.number}
                      </Link>
                    </td>
                    <td>
                      <Icon type={item.icon} />
                      <FormattedMessage id={item.message} />
                    </td>
                  </tr>))
                }
              </table>
            </div>
          </div>
          {/*dev-pipeline*/}
          {this.getDevPipeline()}
          {/*codeQuality card*/}
          <div className="c7n-dc-card-wrap c7n-dc-card-codeQuality">
            <div className="c7n-dc-card-title">
              <Icon type="quality" />
              <FormattedMessage id="codeQuality.content.title" />
            </div>
            <div className="c7n-card-codeQuality-content">
              <div className="codeQuality-content-block">
                <span className={`codeQuality-head-status codeQuality-head-status-success`}>
                  {formatMessage({ id: "success" })}
                </span>
                <FormattedMessage id="codeQuality.content.title" />
              </div>
              {_.map(qualityData, ({ title, number, icon, rating}) => (
                <div className="codeQuality-content-block" key={title}>
                  <div className="codeQuality-content-block-detail mg-bottom-12">
                    {!rating && <Percentage data={number} size={30} />}
                    <span className="codeQuality-content-number">{number}</span>
                    {rating && <Rating rating={rating} size="30px" fontSize="20px" />}
                  </div>
                  <div className="codeQuality-content-block-detail">
                    <Icon type={icon} />
                    <span className="mg-left-8">{title}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
          {/*tag card*/}
          <div className="c7n-dc-card-wrap">
            <div className="c7n-dc-card-title">
              <Icon type="local_offer" />
              <FormattedMessage id="apptag.head" />
            </div>
            {(loading || _.isNull(loading)) ? <LoadingBar display /> : <Fragment>
              {tagList.length ? <Fragment>
                <Collapse className="c7n-dc-collapse-padding" bordered={false}>{tagList}</Collapse>
                <div className="c7n-tag-pagin">
                  <Pagination
                    total={total}
                    current={current}
                    pageSize={pageSize}
                    onChange={this.handlePaginChange}
                    onShowSizeChange={this.handlePaginChange}
                  />
                </div>
              </Fragment> : (<Fragment>
                {empty === 'tag' ? (
                  <div className="c7n-tag-empty">
                    <Icon type="info" className="c7n-tag-empty-icon" />
                    <span className="c7n-tag-empty-text">{formatMessage({ id: `apptag.${empty}.empty` })}</span>
                    <Button
                      type="primary"
                      funcType="raised"
                      onClick={this.displayModal.bind(this, 'createTag')}
                    >
                      <FormattedMessage id="apptag.create" />
                    </Button>
                  </div>
                ) : null}
              </Fragment>)}
            </Fragment>}
          </div>
        </Content>
          {/*tag delete modal*/}
          <Modal
            confirmLoading={deleteLoading}
            visible={modalDisplay === 'deleteTag'}
            title={`${formatMessage({ id: 'apptag.action.delete' })}“${tagName}”`}
            closable={false}
            footer={[
              <Button key="back" onClick={this.displayModal.bind(this, 'close')} disabled={deleteLoading}>{<FormattedMessage
                id="cancel" />}</Button>,
              <Button key="submit" type="danger" onClick={this.deleteTag} loading={deleteLoading}>
                {formatMessage({ id: 'delete' })}
              </Button>,
            ]}
          >
            <div className="c7n-padding-top_8">{formatMessage({ id: 'apptag.delete.tooltip' })}</div>
          </Modal>
          {/*branch delete modal*/}
        <Modal
          confirmLoading={deleteLoading}
          visible={modalDisplay === 'deleteBranch'}
          title={`${formatMessage({ id: 'branch.action.delete' })}“${branchName}”`}
          closable={false}
          footer={[
            <Button key="back" onClick={this.displayModal.bind(this, 'close')} disabled={deleteLoading}>{<FormattedMessage id="cancel" />}</Button>,
            <Button key="submit" type="danger" onClick={this.deleteBranch} loading={deleteLoading}>
              {formatMessage({ id: 'delete' })}
            </Button>,
          ]}
        >
          <div className="c7n-padding-top_8">{formatMessage({ id: 'branch.delete.tooltip' })}</div>
        </Modal>
        {modalDisplay === 'createTag' ? <AppTagCreate
          app={titleName}
          store={AppTagStore}
          show={modalDisplay === 'createTag'}
          close={this.displayModal.bind(this, 'close')}
        /> : null}
        {modalDisplay === 'editTag' ? <AppTagEdit
          app={currentAppName}
          store={AppTagStore}
          tag={tagName}
          release={tagRelease}
          show={modalDisplay === 'editTag'}
          close={this.displayModal.bind(this, 'close')}
        /> : null}
        {BranchStore.createBranchShow === 'create' ? <BranchCreate
          name={titleName}
          appId={DevPipelineStore.selectedApp}
          store={BranchStore}
          visible={BranchStore.createBranchShow === 'create'}
          onClose={this.displayModal.bind(this, 'closeBranch')}
          isDevConsole
        /> : null}
        {BranchStore.createBranchShow === 'detail' && <IssueDetail
          name={branchName}
          store={BranchStore}
          visible={BranchStore.createBranchShow === 'detail'}
          onClose={this.displayModal.bind(this, 'closeBranch')}
          isDevConsole
        />}
        {BranchStore.createBranchShow === 'edit' && <BranchEdit
          name={branchName}
          appId={DevPipelineStore.selectedApp}
          store={BranchStore}
          visible={BranchStore.createBranchShow === 'edit'}
          onClose={this.displayModal.bind(this, 'closeBranch')}
          isDevConsole
        />}</Fragment> : <DepPipelineEmpty title={<FormattedMessage id="devCs.head" />} type="app" />}
      </Page>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(DevConsole)));
