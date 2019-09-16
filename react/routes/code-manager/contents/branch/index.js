import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Button, Tooltip, Modal, Table } from 'choerodon-ui';
import { Content, Page, Permission, stores, Action } from '@choerodon/master';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import BranchCreate from './branch-create';
import TimePopover from '../../../../components/timePopover';
import BranchEdit from './branch-edit';
import IssueDetail from './issue-detail';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';
import DevPipelineStore from '../../stores/DevPipelineStore';
import StatusIcon from '../../../../components/StatusIcon/StatusIcon';
import BranchStore from './stores';
import handleMapStore from '../../main-view/store/handleMapStore';
import Loading from '../../../../components/loading';
import '../../../main.less';
import './Branch.less';
import './index.less';

const { AppState } = stores;


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
    handleMapStore.setCodeManagerBranch({
      refresh: this.handleRefresh,
      select: this.loadData,
      getSelfToolBar: this.getSelfToolBar,
    });
  }

  componentWillUnmount() {
    
  }

  /**
   * 生成特殊的自定义tool-bar
   * 为选择应用或者该应用是空仓库那么就不显示 创建分支按钮
   */
  getSelfToolBar= () => (
    !(DevPipelineStore.getSelectApp && BranchStore.getBranchList.length > 0)
      ? null
      : <Permission
        service={['devops-service.devops-git.createBranch',
        ]}
      >
        <Button
          onClick={this.showSidebar}
          icon="playlist_add"
          disabled={!DevPipelineStore.getSelectApp}
        >
          <FormattedMessage id="branch.create" />
        </Button>
      </Permission>)
  

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

  renderAction = (record) => {
    const {
      intl: { formatMessage },
    } = this.props;
    const action = [
      {
        service: [
          'devops-service.devops-git.pageBranchByOptions',
        ],
        text: formatMessage({ id: 'branch.request' }),
        action: () => {
          window.open(`${record.commitUrl.split('/commit')[0]}/merge_requests/new?change_branches=true&merge_request[source_branch]=${record.branchName}&merge_request[target_branch]=master`);
        },
      },
      {
        service: [
          'devops-service.devops-git.deleteBranch',
        ],
        text: formatMessage({ id: 'delete' }),
        action: () => {
          this.openRemove(record.branchName);
        },
      },
    ];
    // 分支如果是master  禁止创建合并请求 否认：会造成跳转到 gitlab，gailab页面报错的问题
    if (record.branchName === 'master') {
      action.shift(); 
    }

    // 如果仅有一个分支那么禁止删除
    if (BranchStore.getBranchList.length <= 1) {
      // 如果 仅有一个分支 且分支是master 那么禁止做任何操作
      if (record.branchName === 'master') {
        return null;
      }
      action.pop();
    }
    return (<Action data={action} />);
  };


  getBranchColumn = () => {
    const { paras, filters, sort: { columnKey, order } } = this.state;
    return [
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
            handleAtagClick={this.handleEdit}
          />
        </div>),
      }, {
        key: 'action',
        align: 'right',
        width: 60,
        render: this.renderAction,
      }, {
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
    ];
  }

  /**
   * 获取分支列表正文
   * @returns {*}
   */
  get tableBranch() {
    const { intl: { formatMessage } } = this.props;
    
    const menu = AppState.currentMenuType;

    const { getBranchList, loading, getPageInfo } = BranchStore;
    return (
      <div>
        <Table
          filterBarPlaceholder={formatMessage({ id: 'filter' })}
          loading={loading}
          className="c7n-branch-table"
          pagination={getPageInfo}
          columns={this.getBranchColumn()}
          dataSource={getBranchList}
          rowKey={({ creationDate, branchName }) => `${branchName}-${creationDate}`}
          onChange={this.tableChange}
          locale={{ emptyText: formatMessage({ id: 'branch.empty' }) }}
          noFilter
        />
      </div>

    );
  }

  /**
   * 获取分支
   */
  loadData = () => {
    const { projectId } = this.state;
    BranchStore.loadBranchList({ projectId });
  };

  /**
   * 修改相关联问题
   * @param name
   */
  handleEdit = (name) => {
    this.setState({ name });
    BranchStore.loadBranchByName(this.state.projectId, DevPipelineStore.selectedApp, name);
    BranchStore.setCreateBranchShow('edit');
  };

  /**
   * 刷新
   */
  handleRefresh = () => {
    const pagination = BranchStore.getPageInfo;
    const { filters, paras, sort } = this.state;
    this.tableChange(pagination, filters, sort, paras);
  };

  /**
   * 创建分支的弹框
   */
  showSidebar = () => {
    const { projectId } = this.state;
    BranchStore.loadTagData(projectId);
    BranchStore.loadBranchData({
      projectId,
      size: 3,
    });
    BranchStore.setCreateBranchShow('create');
  };

  showIssue = (id, name) => {
    this.setState({ name });
    BranchStore.loadIssueById(this.state.projectId, id);
    BranchStore.loadIssueTimeById(this.state.projectId, id);
    BranchStore.setCreateBranchShow('detail');
  };

  /**
   * 关闭sidebar
   */
  hideSidebar = (isload = true) => {
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
      this.closeRemove();
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
    const menu = AppState.currentMenuType;
    const organizationId = menu.id;
    this.setState({ filters, paras, sort: sorter });
    const page = pagination.current;
    const sort = { field: 'creation_date', order: 'asc' };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === 'ascend') {
        sort.order = 'asc';
      } else if (sorter.order === 'descend') {
        sort.order = 'desc';
      }
    }

    const searchParam = {};
    if (filters) {
      _.forEach(filters, (value, key) => {
        searchParam[key] = value[0];
      });
    }
    const postData = {
      searchParam,
      params: paras,
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
    const { intl: { formatMessage }, history: { location: { state } } } = this.props;
    const { name: branchName, submitting, visible } = this.state;
    const apps = DevPipelineStore.appData.slice();
    const appId = DevPipelineStore.getSelectApp;
    return (
      <Page
        className="c7n-region c7n-branch"
        service={[
          'devops-service.devops-git.createBranch',
          'devops-service.devops-git.deleteBranch',
          'devops-service.devops-git.updateBranchIssue',
          'devops-service.devops-git.pageBranchByOptions',
          'devops-service.devops-git.pageTagsByOptions',
        ]}
      > 
        {!(DevPipelineStore.getAppData && DevPipelineStore.getAppData.length > 0) ? <Loading display={DevPipelineStore.getLoading} />
          : <Fragment>
            <Content className="page-content c7n-branch-content">
              {this.tableBranch}
            </Content>
            {apps && apps.length && appId ? <Fragment>
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
              </Modal> 
            </Fragment> : null}
          </Fragment>}
      </Page>
    );
  }
}

export default withRouter(injectIntl(Branch));
