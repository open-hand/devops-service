import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Table, Button, Tooltip, Icon, Select, Popover } from 'choerodon-ui';
import { Content, Header, Page, Permission, stores } from '@choerodon/boot';
import _ from 'lodash';
import TimePopover from '../../../../components/timePopover';
import LoadingBar from '../../../../components/loadingBar';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';
import StatusTags from '../../../../components/StatusTags';
import AppName from '../../../../components/appName';
import EnvOverviewStore from '../../../../stores/project/envOverview';
import DepPipelineEmpty from '../../../../components/DepPipelineEmpty/DepPipelineEmpty';
import { SORTER_MAP } from '../../../../common/Constants';
import LogSidebar from '../logSidebar';
import TermSidebar from '../termSidebar';

import '../../../main.scss';
import './ContainerHome.scss';

const { Option, OptGroup } = Select;
const { AppState } = stores;

@observer
class ContainerHome extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showLog: false,
      showTerm: false,
      // 当前打开的 term 的pod信息
      currentPod: null,
      selectPubPage: 0,
      selectProPage: 0,
      appPubLength: 0,
      appProLength: 0,
      appPubDom: [],
      appProDom: [],
    };
  }

  componentDidMount() {
    this.loadInitData();
  }

  componentWillUnmount() {
    const { ContainerStore } = this.props;
    ContainerStore.setEnvCard([]);
    ContainerStore.setAllData([]);
    ContainerStore.setAppId();
    ContainerStore.setEnvId();
    ContainerStore.setInstanceId();
    ContainerStore.setInstanceData([]);
  }

  /**
   * 打开侧边栏
   * @param record
   * @param type 侧边栏类型
   */
  openSidebar(record, type) {
    this.setState({
      [type]: true,
      currentPod: record,
    });
  };

  /**
   * 关闭侧边栏
   */
  closeSidebar = () => {
    this.setState({
      showTerm: false,
      showLog: false,
      currentPod: null,
    });
  };

  /**
   * 处理刷新函数
   */
  handleRefresh = () => {
    const { ContainerStore } = this.props;
    const { filters, sort, paras } = ContainerStore.getInfo;
    const pagination = ContainerStore.getPageInfo;
    const { projectId } = AppState.currentMenuType;
    const envId = EnvOverviewStore.getTpEnvId;
    const appId = ContainerStore.getAppId;

    ContainerStore.loadAppDataByEnv(projectId, envId);
    appId && ContainerStore.loadInstance(projectId, envId, appId);
    this.tableChange(pagination, filters, sort, paras);
  };

  /**
   * table 操作
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  tableChange = (pagination, filters, sorter, paras) => {
    const { ContainerStore } = this.props;
    const { id } = AppState.currentMenuType;
    const envId = EnvOverviewStore.getTpEnvId;
    const appId = ContainerStore.getAppId;
    const instanceId = ContainerStore.getInstanceId;

    ContainerStore.setInfo({ filters, sort: sorter, paras });

    const sort = { field: '', order: 'desc' };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      sort.order = SORTER_MAP[sorter.order];
    }
    let searchParam = {};
    const page = pagination.current;
    if (Object.keys(filters).length) {
      searchParam = filters;
    }
    const postData = {
      searchParam,
      param: paras.toString(),
    };

    ContainerStore.loadData(
      false,
      id,
      envId,
      appId,
      instanceId,
      page,
      pagination.pageSize,
      sort,
      postData,
    );
  };

  /**
   * 获取行
   */
  getColumn = () => {
    const { type, organizationId, projectId } = AppState.currentMenuType;
    const { ContainerStore } = this.props;
    const {
      filters,
      sort: { columnKey, order },
    } = ContainerStore.getInfo;
    return [
      {
        title: <FormattedMessage id="container.status" />,
        key: 'status',
        sorter: true,
        render: this.getActive,
      },
      {
        title: <FormattedMessage id="container.name" />,
        key: 'name',
        dataIndex: 'name',
        sorter: true,
        filters: [],
        filterMultiple: false,
        filteredValue: filters.name || [],
        render: (test, record) => (
          <div className="c7n-containers-list">
            <Tooltip title={<FormattedMessage id={`ist.${record.ready ? 'y' : 'n'}`} />}>
              <Icon
                type={record.ready ? 'check_circle' : 'cancel'}
                className={`c7n-pod-ready-${record.ready ? 'check' : 'cancel'}`}
              />
            </Tooltip>
            <MouserOverWrapper text={record.name} width={0.16}>
              {record.name}
            </MouserOverWrapper>
          </div>
        ),
      },
      {
        title: <FormattedMessage id="container.header.title" />,
        key: 'containers',
        render: record => this.getContainers(record),
      },
      {
        title: <FormattedMessage id="container.app" />,
        dataIndex: 'app',
        key: 'app',
        render: (text, record) => ([<div
          className="c7n-container-col-inside"
          key="app-name"
        >
          <AppName
            name={record.appName}
            showIcon={!!record.projectId}
            self={record.projectId === Number(projectId)}
            width={0.14}
          />
        </div>,
          <div key="app-version">
            <MouserOverWrapper text={record.appVersion} width={0.16}>
                <span className="c7n-deploy-text_gray">
                  {record.appVersion}
                </span>
            </MouserOverWrapper>
          </div>]),
      },
      {
        title: <FormattedMessage id="ist.head" />,
        dataIndex: 'instanceCode',
        key: 'instanceCode',
        render: text => (
          <MouserOverWrapper text={text} width={0.115}>
            <span>{text}</span>
          </MouserOverWrapper>
        )
      },
      {
        title: <FormattedMessage id="container.ip" />,
        dataIndex: 'ip',
        key: 'ip',
        sorter: true,
        filters: [],
        filterMultiple: false,
        filteredValue: filters.ip || [],
      },
      {
        width: 103,
        title: <FormattedMessage id="container.createTime" />,
        dataIndex: 'creationDate',
        key: 'creationDate',
        sorter: true,
        sortOrder: columnKey === 'creationDate' && order,
        render: (text, record) => <TimePopover content={record.creationDate} />,
      },
      {
        width: 80,
        key: 'action',
        render: (test, record) => ([<Permission
          service={[
            'devops-service.devops-env-pod-container.queryLogByPod',
          ]}
          organizationId={organizationId}
          projectId={projectId}
          type={type}
          key="log"
        >
          <Tooltip title={<FormattedMessage id="container.log" />}>
            <Button
              size="small"
              shape="circle"
              icon="insert_drive_file"
              onClick={this.openSidebar.bind(this, record, 'showLog')}
            />
          </Tooltip>
        </Permission>,
          <Permission
            key="term"
            service={[
              'devops-service.devops-env-pod-container.handleShellByPod',
            ]}
            organizationId={organizationId}
            projectId={projectId}
            type={type}
          >
            <Tooltip title={<FormattedMessage id="container.term" />}>
              <Button
                size="small"
                shape="circle"
                icon="debug"
                onClick={this.openSidebar.bind(this, record, 'showTerm')}
              />
            </Tooltip>
          </Permission>]),
      },
    ];
  };

  /**
   * 获取状态
   * @param text
   * @param status
   * @returns {*}
   */
  getActive = (text, { status }) => {
    const statusStyle = {
      textOverflow: 'ellipsis',
      width: '100%',
      height: 20,
      lineHeight: '20px',
      overflow: 'hidden',
      whiteSpace: 'nowrap',
    };
    const wrapStyle = {
      width: 54,
      verticalAlign: 'bottom',
    };

    const statusMap = {
      Completed: [true, '#00bf96'],
      Running: [false, '#00bf96'],
      Error: [false, '#f44336'],
      Pending: [false, '#ff9915'],
    };

    const [wrap, color] = statusMap[status] || [true, 'rgba(0, 0, 0, 0.36)'];

    return <StatusTags
      ellipsis={wrap ? statusStyle : null}
      color={color}
      name={status}
      style={wrapStyle}
    />;
  };

  /**
   * 获取 容器 列
   * @param containers
   */
  getContainers = ({ containers }) => {
    const node = [];
    let item;
    if (containers && containers.length) {
      item = containers[0];
      _.map(containers, (item, index) => {
        node.push(
          <div className="c7n-container-mt" key={index}>
            <Tooltip title={<FormattedMessage id={`ist.${item.ready ? 'y' : 'n'}`} />}>
              <Icon
                type={item.ready ? 'check_circle' : 'cancel'}
                className={`c7n-pod-ready-${item.ready ? 'check' : 'cancel'}`}
              />
            </Tooltip>
            <span>{item.name}</span>
          </div>,
        );
      });
    }
    return (
      <div className="c7n-containers-list">
        {item && (
          <Fragment>
            <Tooltip title={<FormattedMessage id={`ist.${item.ready ? 'y' : 'n'}`} />}>
              <Icon
                type={item.ready ? 'check_circle' : 'cancel'}
                className={`c7n-pod-ready-${item.ready ? 'check' : 'cancel'}`}
              />
            </Tooltip>
            <MouserOverWrapper text={item.name} width={0.08}>
              {item.name}
            </MouserOverWrapper>
          </Fragment>)
        }
        {node.length > 1 && (
          <Popover
            arrowPointAtCenter
            placement="bottomRight"
            getPopupContainer={triggerNode => triggerNode.parentNode}
            content={<Fragment>{node}</Fragment>}
          >
            <Icon type="expand_more" className="container-expend-icon" />
          </Popover>
        )}
      </div>
    );
  };

  /**
   * 环境选择
   * @param value
   * @param option
   */
  handleEnvSelect = (value, option) => {
    this.setState({ page: 1, pageSize: 10, envName: option.props.children });
    EnvOverviewStore.setTpEnvId(value);
    const { ContainerStore } = this.props;
    ContainerStore.setInfo({
      filters: {},
      sort: { columnKey: 'id', order: 'descend' },
      paras: [],
    });
    const appId = ContainerStore.getAppId;
    const projectId = parseInt(AppState.currentMenuType.id, 10);
    ContainerStore.setInstanceId();
    ContainerStore.loadAppDataByEnv(projectId, value).then(data => {
      const appData = ContainerStore.getAppData;
      if (!_.find(appData, app => app.id === appId)) {
        ContainerStore.setAppId(null);
        ContainerStore.loadData(false, projectId, value, null);
        ContainerStore.setInstanceData([]);
      } else {
        ContainerStore.loadData(false, projectId, value, appId);
        ContainerStore.loadInstance(projectId, value, appId);
      }
    });
  };

  /**
   * 应用选择
   * @param value
   */
  handleAppSelect = value => {
    const { ContainerStore } = this.props;
    ContainerStore.setAppId(value);
    ContainerStore.setInstanceId();
    ContainerStore.setInfo({
      filters: {},
      sort: { columnKey: 'id', order: 'descend' },
      paras: [],
    });
    const envId = EnvOverviewStore.getTpEnvId;
    const projectId = parseInt(AppState.currentMenuType.id, 10);
    ContainerStore.loadData(false, projectId, envId, value);
    if (value) {
      ContainerStore.loadInstance(projectId, envId, value);
    } else {
      ContainerStore.setInstanceData([]);
    }
  };

  /**
   * 展开更多
   * @param type
   * @param e
   */
  appDomMore = (type, e) => {
    e.stopPropagation();
    const { ContainerStore } = this.props;
    const { selectProPage, selectPubPage } = this.state;
    const filterValue = ContainerStore.getFilterValue;
    if (type === 'pro') {
      const temp = selectProPage + 1;
      this.setState({
        selectProPage: temp,
      });
      this.loadSelectData([temp, selectPubPage], filterValue);
    } else {
      const temp = selectPubPage + 1;
      this.setState({
        selectPubPage: temp,
      });
      this.loadSelectData([selectProPage, temp], filterValue);
    }
  };

  /**
   * 加载应用
   * @param pageArr
   * @param filterValue
   */
  loadSelectData = (pageArr, filterValue) => {
    const { ContainerStore } = this.props;
    const projectId = parseInt(AppState.currentMenuType.id, 10);
    const appId = ContainerStore.getAppId;
    const envId = EnvOverviewStore.getTpEnvId;
    const appPubDom = [];
    const appProDom = [];
    let pubLength = 0;
    let proLength = 0;
    envId &&
    ContainerStore.loadAppDataByEnv(projectId, envId, appId).then(data => {
      if (data) {
        const proPageSize = 10 * pageArr[0] + 3;
        const pubPageSize = 10 * pageArr[1] + 3;
        let allItems = data;
        if (filterValue) {
          allItems = data.filter(
            item =>
              item.name.toLowerCase().indexOf(filterValue.toLowerCase()) >= 0,
          );
        }
        if (allItems.length) {
          _.map(allItems, d => {
            if (d.projectId !== projectId) {
              pubLength += 1;
            } else {
              proLength += 1;
            }
            if (d.projectId !== projectId && appPubDom.length < pubPageSize) {
              appPubDom.push(
                <Option key={d.id} value={d.id}>
                  <Popover
                    placement="right"
                    content={
                      <div>
                        <p>
                          <FormattedMessage id="ist.name" />
                          <span>{d.name}</span>
                        </p>
                        <p>
                          <FormattedMessage id="ist.ctr" />
                          <span>{d.contributor}</span>
                        </p>
                        <p>
                          <FormattedMessage id="ist.des" />
                          <span>{d.description}</span>
                        </p>
                      </div>
                    }
                  >
                    <div className="c7n-container-option-popover">
                      <i className="icon icon-apps c7n-container-icon-publish" />
                      <MouserOverWrapper text={d.name} width={0.9}>
                        {d.name}
                      </MouserOverWrapper>
                    </div>
                  </Popover>
                </Option>,
              );
            } else if (appProDom.length < proPageSize) {
              appProDom.push(
                <Option key={d.id} value={d.id}>
                  <Popover
                    placement="right"
                    content={
                      <div>
                        <p>
                          <FormattedMessage id="ist.name" />
                          <span>{d.name}</span>
                        </p>
                        <p>
                          <FormattedMessage id="ist.code" />
                          <span>{d.code}</span>
                        </p>
                      </div>
                    }
                  >
                    <div className="c7n-container-option-popover">
                      <i className="icon icon-project c7n-container-icon-publish" />
                      <MouserOverWrapper text={d.name} width={0.9}>
                        {d.name}
                      </MouserOverWrapper>
                    </div>
                  </Popover>
                </Option>,
              );
            }
          });
        }
        this.setState({
          appPubDom,
          appProDom,
          appPubLength: pubLength,
          appProLength: proLength,
        });
      }
    });
  };

  loadInitData = () => {
    const {
      ContainerStore,
      history: {
        location: { state },
      },
    } = this.props;
    const projectId = parseInt(AppState.currentMenuType.id, 10);
    const { selectProPage, selectPubPage } = this.state;
    const envId = EnvOverviewStore.getTpEnvId;
    let appId = null;
    if (state && state.appId && state.instanceId) {
      appId = Number(state.appId);
      ContainerStore.setInstanceId(Number(state.instanceId));
      ContainerStore.loadInstance(projectId, envId, appId);
    }
    ContainerStore.setAppId(appId);
    EnvOverviewStore.loadActiveEnv(projectId, 'container').then(() =>
      this.loadSelectData([selectProPage, selectPubPage], ''),
    );
  };

  /**
   * 搜索选择应用
   * @param value
   */
  handleFilter = value => {
    const { ContainerStore } = this.props;
    ContainerStore.setFilterValue(value);
    this.setState({
      selectPubPage: 0,
      selectProPage: 0,
    });
    this.loadSelectData([0, 0], value);
  };

  /**
   * 选择实例
   * @param value
   */
  handleIstSelect = value => {
    const { ContainerStore } = this.props;
    const { projectId } =AppState.currentMenuType;
    const envId = EnvOverviewStore.getTpEnvId;
    const appId = ContainerStore.getAppId;
    ContainerStore.setInstanceId(value);
    ContainerStore.loadData(false, projectId, envId, appId, value);
  };

  render() {
    const {
      ContainerStore,
      intl: { formatMessage },
      history: {
        location: { state },
      },
    } = this.props;
    const {
      showLog,
      showTerm,
      currentPod,
      selectProPage,
      selectPubPage,
      appProDom,
      appPubDom,
      appProLength,
      appPubLength,
    } = this.state;
    const envData = EnvOverviewStore.getEnvcard;
    const envId = EnvOverviewStore.getTpEnvId;
    const backPath = state && state.backPath;
    const { paras } = ContainerStore.getInfo;
    const proPageSize = 10 * selectProPage + 3;
    const pubPageSize = 10 * selectPubPage + 3;
    const serviceData =
      ContainerStore.getAllData && ContainerStore.getAllData.slice();
    const instanceData = ContainerStore.getInstanceData;
    const instanceId = instanceData && instanceData.length ? ContainerStore.getInstanceId : undefined;
    const projectName = AppState.currentMenuType.name;
    const initApp =
      envData && envData.length && (appProDom.length || appPubDom.length)
        ? ContainerStore.getAppId || undefined : undefined;
    let tempApp = null;
    if (appProDom.filter(i => parseInt(i.key) === initApp).length === 0 && appPubDom.filter(i => parseInt(i.key) === initApp).length === 0 && initApp) {
      const appData = ContainerStore.getAppData;
      const app = appData.filter(item => item.id === initApp)[0];
      const projectId = parseInt(AppState.currentMenuType.id, 10);
      tempApp = (<Option key={app.id} value={app.id}>
        <Popover
          placement="right"
          content={
            <div>
              <p>
                <FormattedMessage id="ist.name" />
                <span>{app.name}</span>
              </p>
              <p>
                <FormattedMessage id="ist.code" />
                <span>{app.code}</span>
              </p>
            </div>
          }
        >
          <div className="c7n-container-option-popover">
            <i
              className={`icon ${app.projectId === projectId ? 'icon-project' : 'icon-apps'} c7n-container-icon-publish`} />
            <MouserOverWrapper text={app.name} width={0.9}>
              {app.name}
            </MouserOverWrapper>
          </div>
        </Popover>
      </Option>);
    } else {
      tempApp = null;
    }
    const contentDom =
      envData && envData.length && envId ? (
        <React.Fragment>
          <Header
            title={<FormattedMessage id="container.header.title" />}
            backPath={backPath}
          >
            <Select
              className={`${
                envId
                  ? 'c7n-header-select'
                  : 'c7n-header-select c7n-select_min100'
                }`}
              dropdownClassName="c7n-header-env_drop"
              dropdownMatchSelectWidth
              placeholder={formatMessage({ id: 'envoverview.noEnv' })}
              value={envData && envData.length ? envId : undefined}
              disabled={envData && envData.length === 0}
              onChange={this.handleEnvSelect}
            >
              {_.map(envData, e => (
                <Option
                  key={e.id}
                  value={e.id}
                  disabled={!e.permission}
                  title={e.name}
                >
                  <Tooltip placement="right" title={e.name}>
                    <span className="c7n-ib-width_100">
                      {e.connect ? (
                        <span className="c7ncd-status c7ncd-status-success" />
                      ) : (
                        <span className="c7ncd-status c7ncd-status-disconnect" />
                      )}
                      {e.name}
                    </span>
                  </Tooltip>
                </Option>
              ))}
            </Select>
            <Button
              icon="refresh"
              onClick={this.handleRefresh}
            >
              <FormattedMessage id="refresh" />
            </Button>
          </Header>
          <Content
            className="page-content c7n-container-wrapper"
            code="container"
            values={{ name: projectName }}
          >
            <Select
              className="c7n-app-select_247"
              label={formatMessage({ id: 'chooseApp' })}
              value={initApp}
              optionFilterProp="children"
              onChange={this.handleAppSelect}
              filterOption={false}
              onFilterChange={this.handleFilter}
              filter
              allowClear
            >
              <OptGroup label={formatMessage({ id: 'project' })} key="proGroup">
                {appProDom}
                {proPageSize < appProLength && (
                  <Option
                    disabled
                    className="c7ncd-more-btn-wrap"
                    key="pro_more"
                  >
                    <Button
                      className="c7ncd-more-btn"
                      onClick={this.appDomMore.bind(this, 'pro')}
                    >
                      {formatMessage({ id: 'loadMore' })}
                    </Button>
                  </Option>
                )}
              </OptGroup>
              <OptGroup label={formatMessage({ id: 'market' })} key="pubGroup">
                {appPubDom}
                {pubPageSize < appPubLength && (
                  <Option
                    disabled
                    className="c7ncd-more-btn-wrap"
                    key="pub_more"
                  >
                    <Button
                      className="c7ncd-more-btn"
                      onClick={this.appDomMore.bind(this, 'pub')}
                    >
                      {formatMessage({ id: 'loadMore' })}
                    </Button>
                  </Option>
                )}
              </OptGroup>
              {tempApp}
            </Select>
            <Select
              className='c7n-app-select_247'
              label={formatMessage({ id: 'container.chooseIst' })}
              value={instanceId}
              notFoundContent={formatMessage({ id: 'container.ist.empty' })}
              optionFilterProp="children"
              filter
              allowClear
              onChange={this.handleIstSelect}
              filterOption={(input, option) =>
                option.props.children
                  .toLowerCase()
                  .indexOf(input.toLowerCase()) >= 0
              }
            >
              {_.map(instanceData, ({ code, id }) => (
                <Option
                  key={Number(id)}
                  value={Number(id)}
                >
                  {code}
                </Option>
              ))}
            </Select>
            <Table
              filterBarPlaceholder={formatMessage({ id: 'filter' })}
              loading={ContainerStore.loading}
              pagination={ContainerStore.pageInfo}
              columns={this.getColumn()}
              dataSource={serviceData}
              rowKey={record => record.id}
              onChange={this.tableChange}
              filters={paras.slice()}
            />
          </Content>
        </React.Fragment>
      ) : (
        <DepPipelineEmpty
          title={<FormattedMessage id="container.header.title" />}
          type="env"
        />
      );

    return (
      <Page
        className="c7n-region"
        service={[
          'devops-service.devops-env-pod.pageByOptions',
          'devops-service.devops-env-pod-container.queryLogByPod',
          'devops-service.devops-env-pod-container.handleShellByPod',
        ]}
      >
        {ContainerStore.isRefresh ? <LoadingBar display /> : contentDom}
        {showLog && <LogSidebar
          onClose={this.closeSidebar}
          current={currentPod}
          visible={showLog}
          store={ContainerStore}
        />}
        {showTerm && <TermSidebar
          onClose={this.closeSidebar}
          current={currentPod}
          visible={showTerm}
          store={ContainerStore}
        />}
      </Page>
    );
  }
}

export default withRouter(injectIntl(ContainerHome));
