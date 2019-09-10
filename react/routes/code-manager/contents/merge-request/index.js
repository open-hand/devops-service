import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Button, Tabs, Icon, Select, Table, Tooltip } from 'choerodon-ui';
import { Content, Header, Page, Permission, stores } from '@choerodon/master';
import { injectIntl, FormattedMessage } from 'react-intl';
import TimeAgo from 'timeago-react';
import _ from 'lodash';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';
import StatusIcon from '../../../../components/StatusIcon/StatusIcon';
import DevPipelineStore from '../../stores/DevPipelineStore';
import Tips from '../../../../components/Tips';
import MergeRequestStore from './stores';
import handleMapStore from '../../main-view/store/handleMapStore';
import Loading from '../../../../components/loading';
import './index.less';
import '../../../main.less';

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
      getSelfToolBar: this.getSelfToolBar,
    });
    this.state = {
      tabKey: 'opened',
    };
  }

  componentDidMount() {
    MergeRequestStore.loadUser();
  }
  

  /**
   * 生成特殊的自定义tool-bar
   */
  getSelfToolBar= () => (
    <Permission
      service={['devops-service.devops-git.queryUrl']}
    >
      <Button
        funcType="flat"
        onClick={this.linkToNewMerge}
        disabled={!MergeRequestStore.getUrl}
      >
        <i className="icon-playlist_add icon" />
        <FormattedMessage id="merge.createMerge" />
      </Button>
    </Permission>)

  /**
   * 刷新函数
   */
  reload = () => {
    this.setState({
      param: [],
    });
    MergeRequestStore.setLoading(true);
    MergeRequestStore.loadMergeRquest(
      DevPipelineStore.getSelectApp,
      this.state.tabKey,
      MergeRequestStore.pageInfo.current,
      MergeRequestStore.pageInfo.pageSize,
    );
    MergeRequestStore.loadUrl(AppState.currentMenuType.id, DevPipelineStore.getSelectApp);
  };

  tabChange = (key) => {
    this.setState({
      tabKey: key,
    });
    let keys = key;
    if (key === 'assignee') {
      keys = 'opened';
    }
    MergeRequestStore.loadMergeRquest(DevPipelineStore.getSelectApp, keys);
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
      DevPipelineStore.getSelectApp,
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
      title: <FormattedMessage id="app.name" />,
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (<StatusIcon
        name={record.title}
        width={0.25}
        handleAtagClick={this.linkToMerge.bind(this, record.iid)}
      />),
    }, {
      title: <Tips type="title" data="app.code" />,
      key: 'iid',
      render: (record) => (<span>!{record.iid}</span>),
    }, {
      title: <Tips type="title" data="app.branch" />,
      key: 'targetBranch',
      render: (record) => (
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
      render: (record) => (
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
      render: (record) => (
        <div>
          {record.commits && record.commits.length ? `${record.commits.length} commits` : '0 commit'}
        </div>),
    }, {
      title: <FormattedMessage id="merge.upDate" />,
      key: 'updatedAt',
      render: (record) => (
        <div>
          <Tooltip title={record.updatedAt}>
            <TimeAgo
              datetime={record.updatedAt}
              locale={this.props.intl.formatMessage({ id: 'language' })}
            />
          </Tooltip>
        </div>),
    }];

    const columns = [{
      title: <FormattedMessage id="app.name" />,
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (<StatusIcon
        name={record.title}
        width={0.25}
        handleAtagClick={this.linkToMerge.bind(this, record.iid)}
      />),
    }, {
      title: <Tips type="title" data="app.code" />,
      key: 'iid',
      render: (record) => (<span>!{record.iid}</span>),
    }, {
      title: <FormattedMessage id="app.name" />,
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (<StatusIcon
        name={record.title}
        width={0.25}
        handleAtagClick={this.linkToMerge.bind(this, record.iid)}
      />),
    }, {
      title: <Tips type="title" data="app.branch" />,
      key: 'targetBranch',
      render: (record) => (
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
      render: (record) => (
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
      render: (record) => (
        <div>
          {record.commits && record.commits.length ? `${record.commits.length} commits` : '0 commit'}
        </div>),
    }, {
      title: <FormattedMessage id="merge.upDate" />,
      key: 'updatedAt',
      render: (record) => (
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
    }];

    const columnsOpen = [{
      title: <FormattedMessage id="app.name" />,
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (
        <StatusIcon
          name={record.title}
          width={0.25}
          handleAtagClick={this.linkToMerge.bind(this, record.iid)}
        />),
    }, {
      title: <Tips type="title" data="app.code" />,
      key: 'iid',
      render: (record) => (<span>!{record.iid}</span>),
    }, {
      title: <Tips type="title" data="app.branch" />,
      key: 'targetBranch',
      render: (record) => (
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
      render: (record) => (
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
      render: (record) => (
        <div>
          {record.commits && record.commits.length ? `${record.commits.length} commits` : '0 commit'}
        </div>),
    }, {
      title: <FormattedMessage id="merge.upDate" />,
      key: 'updatedAt',
      render: (record) => (
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
      render: (record) => (
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
    }];

    const hasAppData = appData && appData.length;

    return (
      <Page
        className="c7n-region page-container c7n-merge-wrapper"
        service={[
          'devops-service.devops-git.listMergeRequest',
          'devops-service.devops-git.queryUrl',
        ]}
      >
        {hasAppData && appId
          ? <Fragment>
            <Content values={{ name: titleName }} className="c7n-merge-content">
              <Tabs activecKey={tabKey} onChange={this.tabChange} animated={false} className="c7n-merge-tabs" type="card" size="small" tabBarStyle={{ marginRight: '0' }}>
                <TabPane tab={`${intl.formatMessage({ id: 'merge.tab1' })}(${openCount || 0})`} key="opened">
                  <Table
                    filterBarPlaceholder={intl.formatMessage({ id: 'filter' })}
                    onChange={this.tableChange}
                    loading={getIsLoading}
                    columns={columnsOpen}
                    pagination={false}
                    filters={param || []}
                    dataSource={od.slice()}
                    rowKey={(record) => record.id}
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
                    rowKey={(record) => record.id}
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
                    rowKey={(record) => record.id}
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
                    rowKey={(record) => record.id}
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
                      rowKey={(record) => record.id}
                      filterBar={false}
                    />
                  </TabPane> : null}
              </Tabs>
            </Content>
          </Fragment>
          : <Loading display={DevPipelineStore.getLoading} />}
      </Page>
    );
  }
}

export default withRouter(injectIntl(MergeRequestHome));
