import React, {Component, Fragment} from 'react';
import { withRouter } from 'react-router-dom';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content, Header, Page, Permission, stores } from '@choerodon/boot';
import { Icon, Button, Table, Tooltip } from 'choerodon-ui';
import { CopyToClipboard } from 'react-copy-to-clipboard';
import _ from 'lodash';
import MouserOverWrapper from '../../../../components/MouseOverWrapper/index';
import '../../../main.scss';
import './RepositoryHome.scss';
import DepPipelineEmpty from "../../../../components/DepPipelineEmpty/DepPipelineEmpty";
import DevPipelineStore from "../../../../stores/project/devPipeline";

const { AppState } = stores;
const repoColor = [
  '#45A3FC',
  '#FFB100',
  '#F44336',
  '#00BFA5',
  '#AF4CFF',
  '#F953BA',
];

@observer
class RepositoryHome extends Component {
  constructor(props) {
    super(props);
    const menu = AppState.currentMenuType;
    this.state = {
      projectId: menu.id,
      param: [],
      filters: {},
      sort: {
        columnKey: 'id',
        order: 'descend',
      },
    };
  }

  componentDidMount() {
    DevPipelineStore.queryAppData(AppState.currentMenuType.id);
    this.loadRepoData();
  }

  /**
   * @param id
   * @returns {string}
   */
  getRepoColor = (id) => {
    const idMode = id % 6;
    return repoColor[idMode];
  };

  /**
   * 表格切换页码和搜索排序时触发
   * @param pagination
   * @param filters
   * @param sorter
   * @param param
   */
  tableChange = (pagination, filters, sorter, param) => {
    const search = {
      searchParam: filters,
      param: param[0],
    };
    this.setState({ param, filters, sort: sorter });
    this.loadRepoData(pagination.current - 1, pagination.pageSize, sorter, search);
  };

  /**
   * 分页加载所有仓库数据
   * @param page
   * @param pageSize
   * @param sorter
   * @param search
   */
  loadRepoData = (page = 0, pageSize, sorter = {}, search = { searchParam: {}, param: '' }) => {
    const { RepositoryStore } = this.props;
    const { projectId } = this.state;
    RepositoryStore.queryRepoData(projectId, page, pageSize, sorter, search);
  };

  /**
   * 页面刷新
   */
  handleRefresh = () => {
    const { RepositoryStore } = this.props;
    const { param, sort, filters } = this.state;
    const pageInfos = RepositoryStore.getPageInfo;
    this.tableChange(pageInfos, filters, sort, param);
  };

  /**
   * 点击复制代码成功回调
   * @returns {*|string}
   */
  handleCopy = () => Choerodon.prompt('复制成功');

  /**
   * 点击跳转到应用提交情况报表
   * @param appId 应用id
   */
  linkToReports = (appId) => {
    const { history } = this.props;
    const { id: projectId, name: projectName, organizationId, type } = AppState.currentMenuType;
    history.push({
      pathname: '/devops/reports/submission',
      search: `?type=${type}&id=${projectId}&name=${encodeURIComponent(projectName)}&organizationId=${organizationId}`,
      state: {
        appId: [appId],
        backPath: `/devops/repository?type=${type}&id=${projectId}&name=${projectName}&organizationId=${organizationId}`,
      },
    });
  };

  renderAction = (text, record) => {
    const noRepoUrl = this.props.intl.formatMessage({ id: 'repository.noUrl' });
    return (<div>
      { record.sonarUrl ? <Tooltip title={<FormattedMessage id="repository.quality" />} placement="bottom">
        <a className="repo-copy-btn" href={record.sonarUrl} rel="nofollow me noopener noreferrer" target="_blank">
          <Button shape="circle" size="small" icon="quality" />
        </a>
      </Tooltip> : null }
      <Tooltip title={<FormattedMessage id="repository.report" />} placement="bottom">
        <Button
          className="repo-copy-btn"
          shape="circle"
          size="small"
          icon="exit_to_app"
          onClick={this.linkToReports.bind(this, record.id)}
        />
      </Tooltip>
      <Tooltip title={<FormattedMessage id="repository.copyUrl" />} placement="bottom">
        <CopyToClipboard
          text={record.repoUrl || noRepoUrl}
          onCopy={this.handleCopy}
        >
          <Button shape="circle" size="small">
            <i className="icon icon-library_books" />
          </Button>
        </CopyToClipboard>
      </Tooltip>
    </div>);
  };

  render() {
    const { intl, RepositoryStore } = this.props;
    const { type, id: projectId, organizationId: orgId, name } = AppState.currentMenuType;
    const { param, filters, sort: { columnKey, order } } = this.state;
    const { getRepoData, getPageInfo, loading } = RepositoryStore;
    const appData = DevPipelineStore.getAppData;
    const flag = _.filter(appData, ['permission', true]);
    const columns = [{
      title: <FormattedMessage id="repository.repository" />,
      dataIndex: 'code',
      key: 'code',
      sorter: true,
      filters: [],
      sortOrder: columnKey === 'code' && order,
      filteredValue: filters.code || [],
      render: (text, record) => (<div>
        <span className="repo-commit-avatar" style={{ color: this.getRepoColor(record.id) }}>{text.toString().substr(0, 1).toUpperCase()}</span>
        <span>{text}</span>
      </div>),
    }, {
      title: <FormattedMessage id="repository.url" />,
      dataIndex: 'repoUrl',
      key: 'repoUrl',
      render: (text, record) => (<a href={record.repoUrl || null} rel="nofollow me noopener noreferrer" target="_blank">
        <MouserOverWrapper text={record.repoUrl} width={0.25}>
          {record.repoUrl ? `../${record.repoUrl.split('/')[record.repoUrl.split('/').length - 1]}` : ''}
        </MouserOverWrapper>
      </a>),
    }, {
      title: <FormattedMessage id="repository.application" />,
      dataIndex: 'name',
      key: 'name',
      sorter: true,
      filters: [],
      sortOrder: columnKey === 'name' && order,
      filteredValue: filters.name || [],
    }, {
      align: 'right',
      width: 120,
      key: 'action',
      render: this.renderAction,
    }];
    return (
      <Page
        className="c7n-region c7n-app-wrapper"
        service={[
          'devops-service.application.listCodeRepository',
        ]}
      >
        {flag && flag.length ? <Fragment>
          <Header title={<FormattedMessage id="repository.head" />}>
            <Button
              icon='refresh'
              onClick={this.handleRefresh}
            >
              <FormattedMessage id="refresh" />
            </Button>
          </Header>
          <Content code="repository" values={{ name }}>
            <Table
              filterBarPlaceholder={intl.formatMessage({ id: 'filter' })}
              loading={loading}
              onChange={this.tableChange}
              pagination={getPageInfo}
              columns={columns}
              filters={param || []}
              dataSource={getRepoData}
              rowKey={record => record.id}
            />
          </Content>
        </Fragment> : <DepPipelineEmpty title={<FormattedMessage id="repository.head" />} type="app" />}
      </Page>
    );
  }
}

export default injectIntl(withRouter(RepositoryHome));
