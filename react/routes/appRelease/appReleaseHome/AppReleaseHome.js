import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Button,
  Table,
  Popover,
  Modal,
  Tabs,
  Tooltip,
  Icon,
} from 'choerodon-ui';
import {
  Content,
  Header,
  Page,
  Permission,
  stores,
} from '@choerodon/boot';
import '../../main.scss';
import editReleaseStore from '../../../../stores/project/appRelease/editRelease';
import AppVersionStore from '../../../../stores/project/applicationVersion';
import DepPipelineEmpty from '../../../../components/DepPipelineEmpty/DepPipelineEmpty';

const TabPane = Tabs.TabPane;
const { AppState } = stores;
const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;

@observer
class AppReleaseHome extends Component {
  constructor(props) {
    const menu = AppState.currentMenuType;
    super(props);
    this.state = {
      projectId: menu.id,
      key: props.match.params.key === '2' ? '2' : '1',
      paras: [],
      filters: {},
      sorter: {
        columnKey: 'id',
        order: 'descend',
      },
      pageSize: HEIGHT <= 900 ? 10 : 15,
    };
  }

  componentDidMount() {
    const { AppReleaseStore } = this.props;
    const { projectId, key } = this.state;
    AppVersionStore.queryAppData(projectId);
    AppReleaseStore.loadData({ projectId, key });
  }

  getColumn = () => {
    const { type, organizationId } = AppState.currentMenuType;
    const {
      filters,
      sorter: { columnKey, order },
    } = this.state;
    return [
      {
        title: <FormattedMessage id="app.name" />,
        dataIndex: 'name',
        key: 'name',
        sorter: true,
        sortOrder: columnKey === 'name' && order,
        filters: [],
        filteredValue: filters.name || [],
      },
      {
        title: <FormattedMessage id="app.code" />,
        dataIndex: 'code',
        key: 'code',
        sorter: true,
        sortOrder: columnKey === 'code' && order,
        filters: [],
        filteredValue: filters.code || [],
      },
      {
        title: <FormattedMessage id="release.column.level" />,
        key: 'publishLevel',
        sorter: true,
        sortOrder: columnKey === 'publishLevel' && order,
        filters: [
          {
            text: this.props.intl.formatMessage({ id: 'public' }),
            value: 2,
          },
          {
            text: this.props.intl.formatMessage({ id: 'organization' }),
            value: 1,
          },
        ],
        filteredValue: filters.publishLevel || [],
        render: record => (
          <span>
            {record.publishLevel && (
              <FormattedMessage id={`${record.publishLevel}`} />
            )}
          </span>
        ),
      },
      {
        align: 'right',
        key: 'action',
        render: record => (
          <div>
            <Permission
              type={type}
              organizationId={organizationId}
              service={['devops-service.application-market.update']}
            >
              <Tooltip
                trigger="hover"
                placement="bottom"
                title={
                  <div>{this.props.intl.formatMessage({ id: 'edit' })}</div>
                }
              >
                <Button
                  shape="circle"
                  size="small"
                  onClick={this.handleEdit.bind(this, record.id)}
                >
                  <Icon type="mode_edit" />
                </Button>
              </Tooltip>
            </Permission>
            <Permission
              type={type}
              organizationId={organizationId}
              service={['devops-service.application-market.updateVersions']}
            >
              <Tooltip
                trigger="hover"
                placement="bottom"
                title={
                  <div>
                    {this.props.intl.formatMessage({
                      id: 'release.action.version',
                    })}
                  </div>
                }
              >
                <Button
                  shape="circle"
                  size="small"
                  onClick={this.handleEditVersion.bind(this, record)}
                >
                  <Icon type="versionline" />
                </Button>
              </Tooltip>
            </Permission>
          </div>
        ),
      },
    ];
  };

  /**
   * 修改基本信息
   * @param ids
   */
  handleEdit = ids => {
    const { name, id, organizationId } = AppState.currentMenuType;
    this.props.history.push(
      `/devops/app-release/edit/${ids}?type=project&id=${id}&name=${name}&organizationId=${organizationId}`,
    );
  };

  /**
   *发布应用
   * @param record 发布的数据
   */
  handleCreate = record => {
    const { name, id, organizationId } = AppState.currentMenuType;
    editReleaseStore.setAppDetailById(record);
    this.props.history.push(
      `/devops/app-release/add/${
        record.id
        }?type=project&id=${id}&name=${name}&organizationId=${organizationId}`,
    );
  };

  /**
   * 版本控制
   * @param ids
   */
  handleEditVersion = ids => {
    const { name, id, organizationId } = AppState.currentMenuType;
    this.props.history.push(
      `/devops/app-release/app/${ids.name}/edit-version/${
        ids.id
        }?type=project&id=${id}&name=${name}&organizationId=${organizationId}`,
    );
  };

  /**
   * 控制显示为项目下的数据
   * @returns {*}
   */
  showProjectTable = () => {
    const { AppReleaseStore } = this.props;
    const data = AppReleaseStore.getUnReleaseData;
    const {
      paras,
      filters,
      sorter: { columnKey, order },
    } = this.state;

    const column = [
      {
        title: <FormattedMessage id="app.name" />,
        dataIndex: 'name',
        key: 'name',
        sorter: true,
        sortOrder: columnKey === 'name' && order,
        filters: [],
        filteredValue: filters.name || [],
      },
      {
        title: <FormattedMessage id="app.code" />,
        dataIndex: 'code',
        key: 'code',
        sorter: true,
        sortOrder: columnKey === 'code' && order,
        filters: [],
        filteredValue: filters.code || [],
      },
      {
        width: 64,
        key: 'action',
        render: (test, record) => (
          <div>
            <Permission service={['devops-service.application-market.create']}>
              <Tooltip
                placement="bottom"
                title={<FormattedMessage id="release.action.publish" />}
              >
                <Button
                  shape="circle"
                  onClick={this.handleCreate.bind(this, record)}
                >
                  <Icon type="publish2" />
                </Button>
              </Tooltip>
            </Permission>
          </div>
        ),
      },
    ];
    return (
      <Table
        filterBarPlaceholder={this.props.intl.formatMessage({ id: 'filter' })}
        loading={AppReleaseStore.loading}
        pagination={AppReleaseStore.getUnPageInfo}
        columns={column}
        dataSource={data}
        filters={paras}
        rowKey={record => record.id}
        onChange={this.tableChange}
      />
    );
  };

  /**
   * 切换tabs
   * @param value
   */
  handleChangeTabs = value => {
    const { AppReleaseStore } = this.props;
    AppReleaseStore.loadData({
      page: 1,
      key: value,
      projectId: this.state.projectId,
      size: this.state.pageSize,
    });
    this.setState({
      key: value,
      paras: [],
      filters: {},
      sorter: { columnKey: 'id', order: 'descend' },
    });
  };

  /**
   * table 改变的函数
   * @param pagination 分页
   * @param filters 过滤
   * @param sorter 排序
   * @param paras
   */
  tableChange = (pagination, filters, sorter, paras) => {
    const { AppReleaseStore } = this.props;
    const menu = AppState.currentMenuType;
    const organizationId = menu.id;
    const sort = { field: 'id', order: 'desc' };
    this.setState({ paras, filters, sorter });
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === 'ascend') {
        sort.order = 'asc';
      } else if (sorter.order === 'descend') {
        sort.order = 'desc';
      }
    }
    const page = pagination.current;
    let searchParam = {};
    if (Object.keys(filters).length) {
      searchParam = filters;
    }
    const postData = {
      searchParam,
      param: paras.toString(),
    };
    AppReleaseStore.loadData({
      projectId: organizationId,
      sorter: sort,
      postData,
      key: this.state.key,
      page,
      size: pagination.pageSize,
    });
  };

  handleRefresh = () => {
    const { AppReleaseStore } = this.props;
    const pagination = AppReleaseStore.getPageInfo;
    const { paras, filters, sorter } = this.state;
    this.tableChange(pagination, filters, sorter, paras);
  };

  render() {
    const { AppReleaseStore } = this.props;
    const { name } = AppState.currentMenuType;
    const data = AppReleaseStore.getReleaseData;
    const appData = AppVersionStore.getAppData;
    const { paras } = this.state;
    return (
      <Page
        service={[
          'devops-service.application-market.pageListMarketAppsByProjectId',
          'devops-service.application.listByActiveAndPubAndVersion',
          'devops-service.application-market.updateVersions',
          'devops-service.application-market.update',
        ]}
        className="c7n-region"
      >
        {appData && appData.length ? (
          <Fragment>
            <Header title={<FormattedMessage id="release.home.header.title" />}>
              <Button onClick={this.handleRefresh}>
                <Icon type="refresh" />
                <FormattedMessage id="refresh" />
              </Button>
            </Header>
            <Content code="release" values={{ name }}>
              <Tabs
                defaultActiveKey={this.state.key}
                onChange={this.handleChangeTabs}
                animated={false}
              >
                <TabPane
                  tab={<FormattedMessage id="release.home.app.unpublish" />}
                  key="1"
                >
                  {this.showProjectTable()}
                </TabPane>
                <TabPane
                  tab={<FormattedMessage id="release.home.app.publish" />}
                  key="2"
                >
                  <Table
                    filterBarPlaceholder={this.props.intl.formatMessage({
                      id: 'filter',
                    })}
                    loading={AppReleaseStore.loading}
                    pagination={AppReleaseStore.getPageInfo}
                    columns={this.getColumn()}
                    dataSource={data}
                    filters={paras.slice()}
                    rowKey={record => record.id}
                    onChange={this.tableChange}
                  />
                </TabPane>
              </Tabs>
            </Content>
          </Fragment>
        ) : (
          <DepPipelineEmpty
            title={<FormattedMessage id="release.home.header.title" />}
            type="app"
          />
        )}
      </Page>
    );
  }
}

export default withRouter(injectIntl(AppReleaseHome));
