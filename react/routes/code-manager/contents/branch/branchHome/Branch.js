import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Button, Tooltip, Modal, Table, Popover, Select, Icon } from 'choerodon-ui';
import { Content, Header, Page, Permission, stores } from '@choerodon/master';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import '../../main.scss';
import './Branch.scss';
import BranchCreate from '../branchCreate';
import TimePopover from '../../../../components/timePopover';
import BranchEdit from '../branchEdit';
import IssueDetail from '../issueDetail';
import '../index.scss';
import MouserOverWrapper from '../../../components/MouseOverWrapper';
import DevPipelineStore from '../../code-manager/devPipeline';
import StatusIcon from '../../../components/StatusIcon/StatusIcon';

const { AppState } = stores;
const { Option, OptGroup } = Select;

@observer
class Branch extends Component {
  constructor(props) {
    super(props);
    const menu = AppState.currentMenuType;
    this.state = {
      projectId: menu.id,
      paras: [],
      filters: {},
      sort: {
        columnKey: 'creation_date',
        order: 'ascend',
      },
    };
  }

  componentDidMount() {
    const {
      history: { location: { state } },
    } = this.props;
    let historyAppId = null;
    if (state && state.appId) {
      historyAppId = state.appId;
    }
  }

  /**
   * 获取issue的options
   * @param s
   * @returns {*}
   */
  getOptionContent = (s) => {
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
    return (<Tooltip title={mes}>
      <div style={{ color }} className="branch-issue"><i className={`icon icon-${icon}`} /></div>
    </Tooltip>);
  };

  /**
   * 获取列表的icon
   * @param name 分支名称
   * @returns {*}
   */
  getIcon = (name) => {
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
   * 获取分支列表正文
   * @returns {*}
   */
  get tableBranch() {
    const { BranchStore, intl: { formatMessage } } = this.props;
    const { paras, filters, sort: { columnKey, order } } = this.state;
    const menu = AppState.currentMenuType;
    const { type, organizationId: orgId } = menu;
    const branchColumns = [
      {
        title: <FormattedMessage id="branch.name" />,
        dataIndex: 'branchName',
        filters: [],
        filteredValue: filters.branchName,
        sorter: true,
        sortOrder: columnKey === 'branchName' && order,
        render: (text, { branchName, status, errorMessage }) => (<div>
          {this.getIcon(branchName)}
          <StatusIcon
            status={status}
            error={errorMessage}
            name={branchName}
            width={0.2}
          />
        </div>),
      },
      {
        title: <FormattedMessage id="branch.commit" />,
        render: (text, record) => (<div>
          <div>
            <i className="icon icon-point branch-column-icon" />
            <a href={record.commitUrl} target="_blank" rel="nofollow me noopener noreferrer">
              <span>{record.sha && record.sha.slice(0, 8)}</span>
            </a>
            <i
              className="icon icon-schedule branch-col-icon branch-column-icon"
              style={{ paddingLeft: 16, fontSize: 16, marginBottom: 2 }}
            />
            <TimePopover
              content={record.commitDate}
              style={{ display: 'inline-block', color: 'rgba(0, 0, 0, 0.65)' }}
            />
          </div>
          {record.commitUserUrl && record.commitUserName ? <Tooltip title={record.commitUserName}>
            <div className="branch-user-img" style={{ backgroundImage: `url(${record.commitUserUrl})` }} />
          </Tooltip> : <Tooltip title={record.commitUserName}>
            <div className="branch-user-img">{record.commitUserName && record.commitUserName.slice(0, 1)}</div>
          </Tooltip>}
          <MouserOverWrapper text={record.commitContent} width={0.2} className="branch-col-icon">
            {record.commitContent}
          </MouserOverWrapper>
        </div>),
      },
      {
        title: <FormattedMessage id="branch.time" />,
        dataIndex: 'commit.committedDate',
        render: (text, record) => (<div>
          {record.createUserName && record.createUserUrl
            ? <React.Fragment>
              <div className="branch-user-img" style={{ backgroundImage: `url(${record.createUserUrl})` }} />
              <div style={{ display: 'inline-block' }}>
                <span style={{ paddingRight: 5 }}>{record.createUserName}</span>
                {record.createUserName !== record.createUserRealName
                && <span>{record.createUserRealName}</span>}
              </div>
            </React.Fragment>
            : <React.Fragment>
              {record.createUserName ? <div>
                <div
                  className="branch-user-img"
                >{record.createUserRealName && record.createUserRealName.slice(0, 1).toUpperCase()}</div>
                <div style={{ display: 'inline-block' }}>
                  <span style={{ paddingRight: 5 }}>{record.createUserName}</span>
                  {record.createUserName !== record.createUserRealName
                  && <span>{record.createUserRealName}</span>}
                </div>
              </div> : null}
            </React.Fragment>}
        </div>),
      },
      {
        title: <FormattedMessage id="branch.issue" />,
        dataIndex: 'commit.message',
        render: (text, record) => (<div>
          {record.typeCode ? this.getOptionContent(record) : null}
          <a onClick={this.showIssue.bind(this, record.issueId, record.branchName)} role="none"><Tooltip
            title={record.issueName}
          >{record.issueCode}</Tooltip></a>
        </div>),
      },
      {
        align: 'right',
        className: 'operateIcons',
        key: 'action',
        render: (test, record) => (
          <div>
            {record.branchName !== 'master'
              ? <React.Fragment>
                <Permission
                  projectId={this.state.projectId}
                  organizationId={orgId}
                  type={type}
                  service={['devops-service.devops-git.update']}
                >
                  <Tooltip
                    placement="bottom"
                    title={<FormattedMessage id="branch.edit" />}
                  >
                    <Button size="small" shape="circle" onClick={this.handleEdit.bind(this, record.branchName)}>
                      <i className="icon icon-mode_edit" />
                    </Button>
                  </Tooltip>
                </Permission>
                <Tooltip
                  placement="bottom"
                  title={<FormattedMessage id="branch.request" />}
                >
                  <a
                    href={record.commitUrl && `${record.commitUrl.split('/commit')[0]}/merge_requests/new?change_branches=true&merge_request[source_branch]=${record.branchName}&merge_request[target_branch]=master`}
                    target="_blank"
                    rel="nofollow me noopener noreferrer"
                  >
                    <Button size="small" shape="circle">
                      <i className="icon icon-merge_request" />
                    </Button>
                  </a>
                </Tooltip>
                <Permission
                  projectId={this.state.projectId}
                  organizationId={orgId}
                  type={type}
                  service={['devops-service.devops-git.delete']}
                >
                  <Tooltip
                    placement="bottom"
                    title={<FormattedMessage id="delete" />}
                  >
                    <Button size="small" shape="circle" onClick={this.openRemove.bind(this, record.branchName)}>
                      <i className="icon icon-delete_forever" />
                    </Button>
                  </Tooltip>
                </Permission>
              </React.Fragment>
              : null}
          </div>
        ),
      },
    ];
    const titleData = ['master', 'feature', 'bugfix', 'release', 'hotfix', 'custom'];
    const title = (<div className="c7n-header-table">
      <span>
        <FormattedMessage id="branch.list" />
      </span>
      <Popover
        overlayClassName="branch-popover"
        placement="rightTop"
        arrowPointAtCenter
        content={<section>
          {
            _.map(titleData, (item) => (<div className="c7n-branch-block" key={item}>
              <span className={`branch-popover-span span-${item}`} />
              <div className="branch-popover-content">
                <p className="branch-popover-p">
                  <FormattedMessage id={`branch.${item}`} />
                </p>
                <p>
                  <FormattedMessage id={`branch.${item}Des`} />
                </p>
              </div>
            </div>))
          }
        </section>}
      >
        <Icon className="branch-icon-help" type="help" />
      </Popover>
    </div>);
    return (
      <div>
        {title}
        <Table
          filters={paras}
          filterBarPlaceholder={formatMessage({ id: 'filter' })}
          loading={BranchStore.loading}
          className="c7n-branch-table"
          rowClassName="c7n-branch-tr"
          pagination={BranchStore.getPageInfo}
          columns={branchColumns}
          dataSource={BranchStore.getBranchList}
          rowKey={({ creationDate, branchName }) => `${branchName}-${creationDate}`}
          onChange={this.tableChange}
          locale={{ emptyText: formatMessage({ id: 'branch.empty' }) }}
        />
      </div>

    );
  }

  /**
   * 获取分支
   */
  loadData = (value) => {
    const { projectId } = this.state;
    const { BranchStore } = this.props;
    DevPipelineStore.setSelectApp(value);
    DevPipelineStore.setRecentApp(value);
    BranchStore.setBranchData({ list: [] });
    BranchStore.loadBranchList({ projectId });
  };

  /**
   * 修改相关联问题
   * @param name
   */
  handleEdit = (name) => {
    const { BranchStore } = this.props;
    this.setState({ name });
    BranchStore.loadBranchByName(this.state.projectId, DevPipelineStore.selectedApp, name);
    BranchStore.setCreateBranchShow('edit');
  };

  /**
   * 刷新
   */
  handleRefresh = () => {
    const { BranchStore } = this.props;
    const pagination = BranchStore.getPageInfo;
    const { filters, paras, sort } = this.state;
    this.tableChange(pagination, filters, sort, paras);
  };

  /**
   * 创建分支的弹框
   */
  showSidebar = () => {
    const { BranchStore } = this.props;
    const { projectId } = this.state;
    BranchStore.loadTagData(projectId);
    BranchStore.loadBranchData({
      projectId,
      size: 3,
    });
    BranchStore.setCreateBranchShow('create');
  };

  showIssue = (id, name) => {
    const { BranchStore } = this.props;
    this.setState({ name });
    BranchStore.loadIssueById(this.state.projectId, id);
    BranchStore.loadIssueTimeById(this.state.projectId, id);
    BranchStore.setCreateBranchShow('detail');
  };

  /**
   * 关闭sidebar
   */
  hideSidebar = (isload = true) => {
    const { BranchStore } = this.props;
    BranchStore.setCreateBranchShow(false);
    BranchStore.setBranch(null);
    if (isload) {
      this.loadData(DevPipelineStore.selectedApp);
      this.setState({ paras: [], filters: {}, sort: { columnKey: 'creation_date', order: 'ascend' } });
    }
  };

  /**
   * 打开删除框
   * @param name
   */
  openRemove = (name) => {
    this.setState({ visible: true, name });
  };

  /**
   * 关闭删除框
   */
  closeRemove = () => {
    this.setState({ visible: false });
  };

  /**
   * 删除数据
   */
  handleDelete = () => {
    const { BranchStore } = this.props;
    const { name } = this.state;
    const menu = AppState.currentMenuType;
    const organizationId = menu.id;
    this.setState({ submitting: true });
    BranchStore.deleteData(organizationId, DevPipelineStore.getSelectApp, name).then((data) => {
      this.setState({ submitting: false });
      this.loadData(DevPipelineStore.selectedApp);
      this.closeRemove();
    }).catch((error) => {
      this.setState({ submitting: false });
      Choerodon.handleResponseError(error);
    });
    this.setState({ paras: [], filters: {}, sort: { columnKey: 'creation_date', order: 'ascend' } });
  };

  /**
   * table筛选
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  tableChange = (pagination, filters, sorter, paras) => {
    const { BranchStore } = this.props;
    const menu = AppState.currentMenuType;
    const organizationId = menu.id;
    this.setState({ filters, paras, sort: sorter });
    const sort = { field: 'creation_date', order: 'asc' };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === 'ascend') {
        sort.order = 'asc';
      } else if (sorter.order === 'descend') {
        sort.order = 'desc';
      }
    }
    let searchParam = {};
    const page = pagination.current;
    if (Object.keys(filters).length) {
      searchParam = filters;
    }
    if (paras.length) {
      searchParam = { branchName: [paras.toString()] };
    }
    const postData = {
      searchParam,
      param: '',
    };
    BranchStore
      .loadBranchList({
        projectId: organizationId,
        page,
        size: pagination.pageSize,
        sort,
        postData,
      });
  };

  render() {
    const { name } = AppState.currentMenuType;
    const { BranchStore, intl: { formatMessage }, history: { location: { state } } } = this.props;
    const { name: branchName, submitting, visible } = this.state;
    const apps = DevPipelineStore.appData.slice();
    const appId = DevPipelineStore.getSelectApp;
    const titleName = _.find(apps, ['id', appId]) ? _.find(apps, ['id', appId]).name : name;
    const backPath = state && state.backPath;
    return (
      <Page
        className="c7n-region c7n-branch"
        service={[
          'devops-service.application.listByActive',
          'devops-service.devops-git.createBranch',
          'devops-service.devops-git.queryByAppId',
          'devops-service.devops-git.delete',
          'devops-service.devops-git.listByAppId',
          'devops-service.devops-git.getTagList',
          'devops-service.devops-git.update',
          'agile-service.issue.queryIssueByOption',
          'agile-service.issue.queryIssue',
          'agile-service.work-log.queryWorkLogListByIssueId',
        ]}
      >
        {apps && apps.length && appId ? <Fragment><Header
          title={<FormattedMessage id="branch.head" />}
          backPath={backPath}
        >
          <Select
            filter
            className="c7n-header-select"
            dropdownClassName="c7n-header-select_drop"
            placeholder={formatMessage({ id: 'ist.noApp' })}
            value={apps && apps.length ? DevPipelineStore.getSelectApp : undefined}
            disabled={apps.length === 0}
            filterOption={(input, option) => option.props.children.props.children.props.children
              .toLowerCase().indexOf(input.toLowerCase()) >= 0}
            onChange={this.loadData}
          >
            <OptGroup label={formatMessage({ id: 'recent' })} key="recent">
              {
                _.map(DevPipelineStore.getRecentApp, (app) => (
                  <Option
                    key={`recent-${app.id}`}
                    value={app.id}
                    disabled={!app.permission}
                  >
                    <Tooltip title={app.code}><span className="c7n-ib-width_100">{app.name}</span></Tooltip>
                  </Option>))
              }
            </OptGroup>
            <OptGroup label={formatMessage({ id: 'deploy.app' })} key="app">
              {
                _.map(apps, (app, index) => (
                  <Option
                    value={app.id}
                    key={index}
                    disabled={!app.permission}
                  >
                    <Tooltip title={app.code}><span className="c7n-ib-width_100">{app.name}</span></Tooltip>
                  </Option>))
              }
            </OptGroup>
          </Select>
          {BranchStore.getBranchList.length && DevPipelineStore.selectedApp ? <Permission
            service={['devops-service.devops-git.createBranch']}
          >
            <Button
              onClick={this.showSidebar}
              icon="playlist_add"
            >
              <FormattedMessage id="branch.create" />
            </Button>
          </Permission> : null}
          <Button
            onClick={this.handleRefresh}
            icon="refresh"
          >
            <FormattedMessage id="refresh" />
          </Button>
        </Header>
          <Content code={apps.length ? 'branch.app' : 'branch'} values={{ name: titleName }} className="page-content">
            {this.tableBranch}
          </Content>
          {BranchStore.createBranchShow === 'create' && <BranchCreate
            name={_.filter(apps, (app) => app.id === DevPipelineStore.selectedApp)[0].name}
            appId={DevPipelineStore.selectedApp}
            store={BranchStore}
            visible={BranchStore.createBranchShow === 'create'}
            onClose={this.hideSidebar}
          />}
          {BranchStore.createBranchShow === 'edit' && <BranchEdit
            name={branchName}
            appId={DevPipelineStore.selectedApp}
            store={BranchStore}
            visible={BranchStore.createBranchShow === 'edit'}
            onClose={this.hideSidebar}
          />}
          {BranchStore.createBranchShow === 'detail' && <IssueDetail
            name={branchName}
            store={BranchStore}
            visible={BranchStore.createBranchShow === 'detail'}
            onClose={this.hideSidebar}
          />}
          <Modal
            confirmLoading={submitting}
            visible={visible}
            title={`${formatMessage({ id: 'branch.action.delete' })}“${branchName}”`}
            closable={false}
            footer={[
              <Button key="back" onClick={this.closeRemove} disabled={submitting}>{<FormattedMessage
                id="cancel"
              />}</Button>,
              <Button key="submit" type="danger" onClick={this.handleDelete} loading={submitting}>
                {formatMessage({ id: 'delete' })}
              </Button>,
            ]}
          >
            <div className="c7n-padding-top_8">{formatMessage({ id: 'branch.delete.tooltip' })}</div>
          </Modal></Fragment> : null}
      </Page>
    );
  }
}

export default withRouter(injectIntl(Branch));
