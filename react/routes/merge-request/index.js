import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Button, Tabs, Icon, Select, Table, Tooltip } from 'choerodon-ui';
import { Content, Header, Page, Permission, stores } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import TimeAgo from 'timeago-react';
import _ from 'lodash';
import MouserOverWrapper from '../../components/MouseOverWrapper';
import DevPipelineStore from '../devPipeline';
import DepPipelineEmpty from '../../components/DepPipelineEmpty/DepPipelineEmpty';
import Tips from '../../components/Tips';
import MergeRequestStore from './stores';
import handleMapStore from '../code-manager/main-view/store/handleMapStore';

import './index.scss';
import '../main.scss';

const { AppState } = stores;
const { Option, OptGroup } = Select;
const TabPane = Tabs.TabPane;

@observer
class MergeRequestHome extends Component {
  constructor(props) {
    super(props);
    handleMapStore.setCodeManagerMergeRequest({
      refresh: this.reload,
      select: this.handleChange,
      getSelfToolBar: this.getSelfToolBar(),
    });
    this.state = {
      tabKey: 'opened',
    };
  }

  componentDidMount() {
    MergeRequestStore.loadUser();
    this.reload();
  }
  

  /**
   * 生成特殊的自定义tool-bar
   */
  getSelfToolBar= () => {
    const appData = DevPipelineStore.getAppData;
    return appData.length ? (<Button
      funcType="flat"
      onClick={this.linkToNewMerge}
    >
      <i className="icon-playlist_add icon" />
      <FormattedMessage id="merge.createMerge" />
    </Button>) : null;
  }

  /**
   * 刷新函数
   */
  reload = () => {
    this.setState({
      param: [],
    });
    MergeRequestStore.setLoading(true);
    MergeRequestStore.loadMergeRquest(
      DevPipelineStore.selectedApp,
      this.state.tabKey,
      MergeRequestStore.pageInfo.current,
      MergeRequestStore.pageInfo.pageSize,
    );
  };

  tabChange = (key) => {
    this.setState({
      tabKey: key,
    });
    let keys = key;
    if (key === 'assignee') {
      keys = 'opened';
    }
    MergeRequestStore.loadMergeRquest(DevPipelineStore.selectedApp, keys);
  };

  /**
   * table 改变的函数
   * @param pagination 分页
   * @param filters 过滤
   * @param sorter 排序
   * @param param 搜索
   */
  tableChange = (pagination, filters, sorter, param) => {
    this.setState({ param });
    let keys = this.state.tabKey;
    if (keys === 'assignee') {
      keys = 'opened';
    }
    MergeRequestStore.setLoading(true);
    MergeRequestStore.loadMergeRquest(
      DevPipelineStore.selectedApp,
      keys,
      pagination.current,
      pagination.pageSize,
    );
  };

  linkToMerge = (iid) => {
    const url = `${MergeRequestStore.getUrl}/merge_requests/${iid}`;
    window.open(url);
  };

  linkToNewMerge = () => {
    const url = `${MergeRequestStore.getUrl}/merge_requests/new`;
    window.open(url);
  };

  handleChange= (id) => {
    this.setState({
      tabKey: 'opened',
    });
    const projectId = parseInt(AppState.currentMenuType.id, 10);
    DevPipelineStore.setSelectApp(id);
    DevPipelineStore.setRecentApp(id);
    MergeRequestStore.setAssignee([]);
    MergeRequestStore.setAssigneeCount(0);
    MergeRequestStore.loadMergeRquest(id, 'opened');
    MergeRequestStore.loadUrl(projectId, id);
  }

  render() {
    const { type, organizationId, name, id: projectId } = AppState.currentMenuType;
    const { intl, history: { location: { state } } } = this.props;
    const { param, tabKey } = this.state;
    const {
      getPageInfo: { opened, merged: mp, closed: cp, all: ap },
      getMerge: { opened: od, merged: md, closed: cd, all: ad },
      getCount: { closeCount, mergeCount, openCount, totalCount },
      getIsLoading,
      getAssignee,
      getAssigneeCount,
    } = MergeRequestStore;
    const appData = DevPipelineStore.getAppData;
    const appId = DevPipelineStore.getSelectApp;
    const titleName = _.find(appData, ['id', appId]) ? _.find(appData, ['id', appId]).name : name;
    const backPath = state && state.backPath;

    const columnsAll = [{
      title: <Tips type="title" data="app.code" />,
      key: 'iid',
      render: record => (<span>!{record.iid}</span>),
    }, {
      title: <FormattedMessage id="app.name" />,
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (<MouserOverWrapper text={record.title} width={0.2}>
        {record.title}
      </MouserOverWrapper>),
    }, {
      title: <Tips type="title" data="app.branch" />,
      key: 'targetBranch',
      render: record => (
        <div className="c7n-merge-branches">
          <Icon type="branch" />
          <span>{record.sourceBranch}</span>
          <Icon type="keyboard_backspace" className="c7n-merge-right" />
          <Icon type="branch" />
          <span>{record.targetBranch}</span>
        </div>
      ),
    }, {
      title: <FormattedMessage id="merge.state" />,
      dataIndex: 'state',
      key: 'state',
    }, {
      title: <Tips type="title" data="create" />,
      key: 'createdAt',
      render: record => (
        <div>
          {record.author ? (<Tooltip
            title={record.author.username !== record.author.name ? `${record.author.username} ${record.author.name}` : record.author.name}
          >
            {record.author.avatarUrl
              ? <img className="c7n-merge-avatar" src={record.author.avatarUrl} alt="avatar" />
              : <span className="apptag-commit apptag-commit-avatar">{record.author.name.toString().substr(0, 1)}</span>}
          </Tooltip>) : <span className="apptag-commit apptag-commit-avatar">?</span>}
          <Tooltip
            title={record.createdAt}
          >
            <TimeAgo
              className="c7n-merge-time"
              datetime={record.createdAt}
              locale={this.props.intl.formatMessage({ id: 'language' })}
            />
          </Tooltip>
        </div>),
    }, {
      title: <Tips type="title" data="merge.commit" />,
      key: 'commits',
      render: record => (
        <div>
          {record.commits && record.commits.length ? `${record.commits.length} commits` : '0 commit'}
        </div>),
    }, {
      title: <FormattedMessage id="merge.upDate" />,
      key: 'updatedAt',
      render: record => (
        <div>
          <Tooltip title={record.updatedAt}>
            <TimeAgo
              datetime={record.updatedAt}
              locale={this.props.intl.formatMessage({ id: 'language' })}
            />
          </Tooltip>
        </div>),
    }, {
      width: 56,
      key: 'action',
      render: (test, record) => (
        <div>
          <Permission
            service={['devops-service.devops-git.getMergeRequestList']}
            organizationId={organizationId}
            projectId={projectId}
            type={type}
          >
            <Tooltip placement="bottom" title={<FormattedMessage id="merge.detail" />}>
              <Button
                size="small"
                shape="circle"
                onClick={this.linkToMerge.bind(this, record.iid)}
              >
                <i className="icon icon-find_in_page" />
              </Button>
            </Tooltip>
          </Permission>
        </div>
      ),
    }];

    const columns = [{
      title: <Tips type="title" data="app.code" />,
      key: 'iid',
      render: record => (<span>!{record.iid}</span>),
    }, {
      title: <FormattedMessage id="app.name" />,
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (<MouserOverWrapper text={record.title} width={0.25}>
        {record.title}
      </MouserOverWrapper>),
    }, {
      title: <Tips type="title" data="app.branch" />,
      key: 'targetBranch',
      render: record => (
        <div className="c7n-merge-branches">
          <Icon type="branch" />
          <span>{record.sourceBranch}</span>
          <Icon type="keyboard_backspace" className="c7n-merge-right" />
          <Icon type="branch" />
          <span>{record.targetBranch}</span>
        </div>
      ),
    }, {
      title: <Tips type="title" data="create" />,
      key: 'createdAt',
      render: record => (
        <div>
          {record.author ? (<Tooltip
            title={record.author.username !== record.author.name ? `${record.author.username} ${record.author.name}` : record.author.name}
          >
            {record.author.avatarUrl
              ? <img className="c7n-merge-avatar" src={record.author.avatarUrl} alt="avatar" />
              : <span className="apptag-commit apptag-commit-avatar">{record.author.name.toString().substr(0, 1)}</span>}
          </Tooltip>) : <span className="apptag-commit apptag-commit-avatar">?</span>}
          <Tooltip title={record.createdAt}>
            <TimeAgo
              className="c7n-merge-time"
              datetime={record.createdAt}
              locale={this.props.intl.formatMessage({ id: 'language' })}
            />
          </Tooltip>
        </div>),
    }, {
      title: <Tips type="title" data="merge.commit" />,
      key: 'commits',
      render: record => (
        <div>
          {record.commits && record.commits.length ? `${record.commits.length} commits` : '0 commit'}
        </div>),
    }, {
      title: <FormattedMessage id="merge.upDate" />,
      key: 'updatedAt',
      render: record => (
        <div>
          <Tooltip
            title={record.updatedAt}
          >
            <TimeAgo
              datetime={record.updatedAt}
              locale={this.props.intl.formatMessage({ id: 'language' })}
            />
          </Tooltip>
        </div>),
    }, {
      width: 56,
      key: 'action',
      render: (test, record) => (
        <div>
          <Permission
            service={['devops-service.devops-git.getMergeRequestList']}
            organizationId={organizationId}
            projectId={projectId}
            type={type}
          >
            <Tooltip placement="bottom" title={<FormattedMessage id="merge.detail" />}>
              <Button
                size="small"
                shape="circle"
                onClick={this.linkToMerge.bind(this, record.iid)}
              >
                <i className="icon icon-find_in_page" />
              </Button>
            </Tooltip>
          </Permission>
        </div>
      ),
    }];

    const columnsOpen = [{
      title: <Tips type="title" data="app.code" />,
      key: 'iid',
      render: record => (<span>!{record.iid}</span>),
    }, {
      title: <FormattedMessage id="app.name" />,
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (<MouserOverWrapper text={record.title} width={0.25}>
        {record.title}
      </MouserOverWrapper>),
    }, {
      title: <Tips type="title" data="app.branch" />,
      key: 'targetBranch',
      render: record => (
        <div className="c7n-merge-branches">
          <Icon type="branch" />
          <MouserOverWrapper text={record.sourceBranch} width={0.1}>{record.sourceBranch}</MouserOverWrapper>
          <Icon type="keyboard_backspace" className="c7n-merge-right" />
          <Icon type="branch" />
          <span>{record.targetBranch}</span>
        </div>
      ),
    }, {
      title: <Tips type="title" data="create" />,
      key: 'createdAt',
      render: record => (
        <div>
          {record.author ? (<Tooltip
            title={record.author.username !== record.author.name ? `${record.author.username} ${record.author.name}` : record.author.name}
          >
            {record.author.avatarUrl
              ? <img className="c7n-merge-avatar" src={record.author.avatarUrl} alt="avatar" />
              : <span className="apptag-commit apptag-commit-avatar">{record.author.name.toString().substr(0, 1)}</span>}
          </Tooltip>) : <span className="apptag-commit apptag-commit-avatar">?</span>}
          <Tooltip title={record.createdAt}>
            <TimeAgo
              className="c7n-merge-time"
              datetime={record.createdAt}
              locale={this.props.intl.formatMessage({ id: 'language' })}
            />
          </Tooltip>
        </div>),
    }, {
      title: <Tips type="title" data="merge.commit" />,
      key: 'commits',
      render: record => (
        <div>
          {record.commits && record.commits.length ? `${record.commits.length} commits` : '0 commit'}
        </div>),
    }, {
      title: <FormattedMessage id="merge.upDate" />,
      key: 'updatedAt',
      render: record => (
        <div>
          <Tooltip
            title={record.updatedAt}
          >
            <TimeAgo
              datetime={record.updatedAt}
              locale={this.props.intl.formatMessage({ id: 'language' })}
            />
          </Tooltip>
        </div>),
    }, {
      title: <FormattedMessage id="merge.assignee" />,
      key: 'assignee',
      render: record => (
        <div>
          {record.assignee ? (<div>
            <Tooltip
              title={record.assignee.username !== record.assignee.name ? `${record.assignee.username} ${record.assignee.name}` : record.assignee.name}
            >
              {record.assignee.avatarUrl
                ? <img className="c7n-merge-avatar" src={record.assignee.avatarUrl} alt="avatar" />
                : <span
                  className="apptag-commit apptag-commit-avatar"
                >{record.assignee.name.toString().substr(0, 1)}</span>}
            </Tooltip>
            {record.assignee.username !== record.assignee.name ? `${record.assignee.username} ${record.assignee.name}` : record.assignee.name}
          </div>) : <FormattedMessage id="merge.noAssignee" />}
        </div>),
    }, {
      width: 56,
      key: 'action',
      render: (test, record) => (
        <Permission
          service={['devops-service.devops-git.getMergeRequestList']}
          organizationId={organizationId}
          projectId={projectId}
          type={type}
        >
          <Tooltip placement="bottom" title={<FormattedMessage id="merge.detail" />}>
            <Button
              icon="find_in_page"
              size="small"
              shape="circle"
              onClick={this.linkToMerge.bind(this, record.iid)}
            />
          </Tooltip>
        </Permission>
      ),
    }];

    const hasAppData = appData && appData.length;

    return (
      <Page
        className="c7n-region page-container"
        service={[
          'devops-service.application.listByActive',
          'devops-service.devops-git.getMergeRequestList',
          'devops-service.devops-git.getUrl',
        ]}
      >
        {hasAppData && appId
          ? <Fragment>
            {/* <Header
              title={<FormattedMessage id="merge.head" />}
              backPath={backPath}
            >
              <Select
                filter
                className="c7n-header-select"
                dropdownClassName="c7n-header-select_drop"
                placeholder={intl.formatMessage({ id: 'ist.noApp' })}
                value={hasAppData ? DevPipelineStore.getSelectApp : undefined}
                disabled={appData.length === 0}
                filterOption={(input, option) => option.props.children.props.children.props.children
                  .toLowerCase().indexOf(input.toLowerCase()) >= 0}
                onChange={this.handleChange}
              >
                <OptGroup label={intl.formatMessage({ id: 'recent' })} key="recent">
                  {
                    _.map(DevPipelineStore.getRecentApp, app => (
                      <Option
                        key={`recent-${app.id}`}
                        value={app.id}
                        disabled={!app.permission}
                      >
                        <Tooltip title={app.code}><span className="c7n-ib-width_100">{app.name}</span></Tooltip>
                      </Option>))
                  }
                </OptGroup>
                <OptGroup label={intl.formatMessage({ id: 'deploy.app' })} key="app">
                  {
                    _.map(appData, (app, index) => (
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
              {appData.length ? (<Button
                funcType="flat"
                onClick={this.linkToNewMerge}
              >
                <i className="icon-playlist_add icon" />
                <FormattedMessage id="merge.createMerge" />
              </Button>) : null}
              <Button
                funcType="flat"
                onClick={this.reload}
              >
                <i className="icon-refresh icon" />
                <FormattedMessage id="refresh" />
              </Button>
            </Header> */}
            <Content values={{ name: titleName }} className="c7n-merge-content">
              <Tabs activecKey={tabKey} onChange={this.tabChange} animated={false} className="c7n-merge-tabs">
                <TabPane tab={`${intl.formatMessage({ id: 'merge.tab1' })}(${openCount || 0})`} key="opened">
                  <Table
                    filterBarPlaceholder={intl.formatMessage({ id: 'filter' })}
                    onChange={this.tableChange}
                    loading={getIsLoading}
                    columns={columnsOpen}
                    pagination={false}
                    filters={param || []}
                    dataSource={od.slice()}
                    rowKey={record => record.id}
                    filterBar={false}
                  />
                </TabPane>
                <TabPane tab={`${intl.formatMessage({ id: 'merge.tab2' })}(${mergeCount || 0})`} key="merged">
                  <Table
                    filterBarPlaceholder={intl.formatMessage({ id: 'filter' })}
                    onChange={this.tableChange}
                    loading={getIsLoading}
                    columns={columns}
                    pagination={mp}
                    filters={param || []}
                    dataSource={md.slice()}
                    rowKey={record => record.id}
                    filterBar={false}
                  />
                </TabPane>
                <TabPane tab={`${intl.formatMessage({ id: 'merge.tab3' })}(${closeCount || 0})`} key="closed">
                  <Table
                    filterBarPlaceholder={intl.formatMessage({ id: 'filter' })}
                    onChange={this.tableChange}
                    loading={getIsLoading}
                    columns={columns}
                    pagination={cp}
                    filters={param || []}
                    dataSource={cd.slice()}
                    rowKey={record => record.id}
                    filterBar={false}
                  />
                </TabPane>
                <TabPane tab={`${intl.formatMessage({ id: 'merge.tab4' })}(${totalCount || 0})`} key="all">
                  <Table
                    filterBarPlaceholder={intl.formatMessage({ id: 'filter' })}
                    onChange={this.tableChange}
                    loading={getIsLoading}
                    columns={columnsAll}
                    pagination={ap}
                    filters={param || []}
                    dataSource={ad.slice()}
                    rowKey={record => record.id}
                    filterBar={false}
                  />
                </TabPane>
                {getAssigneeCount !== 0
                  ? <TabPane tab={`${intl.formatMessage({ id: 'merge.tab5' })}(${getAssigneeCount || 0})`} key="assignee">
                    <Table
                      filterBarPlaceholder={intl.formatMessage({ id: 'filter' })}
                      onChange={this.tableChange}
                      loading={getIsLoading}
                      columns={columnsOpen}
                      pagination={false}
                      filters={param || []}
                      dataSource={getAssignee.slice()}
                      rowKey={record => record.id}
                      filterBar={false}
                    />
                  </TabPane> : null}
              </Tabs>
            </Content>
          </Fragment>
          : <DepPipelineEmpty title={<FormattedMessage id="merge.head" />} type="app" />}
      </Page>
    );
  }
}

export default withRouter(injectIntl(MergeRequestHome));
