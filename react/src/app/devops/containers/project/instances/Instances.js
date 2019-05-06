import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Table, Select, Tooltip, Pagination, Button, Icon, Modal } from 'choerodon-ui';
import { Action, stores, Content, Header, Page } from '@choerodon/boot';
import _ from 'lodash';
import { handleProptError } from '../../../utils';
import ValueConfig from './ValueConfig';
import UpgradeIst from './UpgradeIst';
import ExpandRow from './components/ExpandRow';
import StatusIcon from '../../../components/StatusIcon';
import UploadIcon from './components/UploadIcon';
import AppName from '../../../components/appName';
import DepPipelineEmpty from '../../../components/DepPipelineEmpty/DepPipelineEmpty';
import Tips from '../../../components/Tips/Tips';
import RefreshBtn from '../../../components/refreshBtn';
import PodStatus from './components/PodStatus/PodStatus';
import DevopsStore from '../../../stores/DevopsStore';
import InstancesStore from '../../../stores/project/instances/InstancesStore';
import EnvOverviewStore from '../../../stores/project/envOverview';
import '../../main.scss';
import './Instances.scss';

const Option = Select.Option;
const { AppState } = stores;

@observer
class Instances extends Component {
  constructor(props) {
    super(props);
    this.state = {
      upgradeVisible: false,
      changeVisible: false,
      deleteLoading: false,
      confirmType: '',
      confirmLoading: false,
      idArr: {},
    };
    this.columnAction = this.columnAction.bind(this);
  }

  componentDidMount() {
    const { InstancesStore } = this.props;
    if (!InstancesStore.getIsCache) {
      const { id: projectId } = AppState.currentMenuType;
      EnvOverviewStore.loadActiveEnv(projectId, 'instance');
    } else {
      InstancesStore.setIsCache(false);
    }
  }

  componentWillUnmount() {
    const { InstancesStore } = this.props;
    if (!InstancesStore.getIsCache) {
      InstancesStore.setAppId(null);
      InstancesStore.setAppNameByEnv([]);
      InstancesStore.clearIst();
      InstancesStore.setIstTableFilter(null);
      InstancesStore.setIstPage(null);
    }
    InstancesStore.setValue(null);
    DevopsStore.clearAutoRefresh();
  }

  /**
   * 页码改变的回调
   * @param page
   * @param size
   */
  onPageChange = (page, size) => {
    const { InstancesStore } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const envId = EnvOverviewStore.getTpEnvId;
    InstancesStore.setAppPage(page);
    InstancesStore.setAppPageSize(size);
    InstancesStore.loadAppNameByEnv(projectId, envId, page - 1, size);
  };

  /**
   * 选择应用后获取实例列表
   * @param appId
   */
  loadDetail = appId => {
    const { InstancesStore } = this.props;
    const currentApp = InstancesStore.getAppId;
    const nextApp = appId !== currentApp && appId;
    InstancesStore.setAppId(nextApp);
    InstancesStore.setIstTableFilter(null);
    InstancesStore.setIstPage(null);
    this.reloadData(true, true, nextApp);
  };

  /**
   * 查看部署详情
   */
  linkDeployDetail = record => {
    const { id, status, code } = record;
    const { InstancesStore } = this.props;
    InstancesStore.setIsCache(true);
    const { history } = this.props;
    const {
      id: projectId,
      name: projectName,
      type,
      organizationId,
    } = AppState.currentMenuType;
    history.push({
      pathname: `/devops/instance/${id}/${status}/detail`,
      search: `?type=${type}&id=${projectId}&name=${encodeURIComponent(
        projectName,
      )}&organizationId=${organizationId}`,
      state: { code },
    });
  };

  /**
   * 查询应用标签及实例列表
   * @param id 环境id
   */
  handleEnvSelect = id => {
    const { id: projectId } = AppState.currentMenuType;
    const { InstancesStore } = this.props;
    const { loadAppNameByEnv, getAppPage, getAppPageSize } = InstancesStore;
    EnvOverviewStore.setTpEnvId(id);
    InstancesStore.setAppId(false);
    loadAppNameByEnv(projectId, id, getAppPage - 1, getAppPageSize);
    this.reloadData(true, true);
  };

  /**
   * table 改变的函数
   * @param pagination 分页
   * @param filters 过滤
   * @param sorter 排序
   * @param param 搜索
   */
  tableChange = (pagination, filters, sorter, param) => {
    const { id: projectId } = AppState.currentMenuType;
    const { InstancesStore } = this.props;
    const { current, pageSize } = pagination;
    const appId = InstancesStore.getAppId;
    const envId = EnvOverviewStore.getTpEnvId;
    const time = Date.now();

    InstancesStore.setIstTableFilter({ filters, param });
    InstancesStore.setIstPage({ page: current - 1, pageSize });
    InstancesStore.loadInstanceAll(true, projectId, {
      envId,
      appId,
    }, time).catch(err => {
      InstancesStore.changeLoading(false);
      Choerodon.handleResponseError(err);
    });
  };

  /**
   * 修改配置实例信息
   */
  updateConfig = async (record) => {
    const { InstancesStore } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const { code, id, envId, commandVersionId, appId } = record;

    this.setState({
      idArr: {
        environmentId: envId,
        appVersionId: commandVersionId,
        appId,
      },
      name: code,
    });
    InstancesStore.setValue(null);
    let res = await InstancesStore.loadValue(projectId, id, commandVersionId);
    if (res) {
      this.setState({ changeVisible: true, id });
    }
  };

  /**
   * 重新部署
   * @param id
   */
  reStart = id => {
    const { id: projectId } = AppState.currentMenuType;
    const envId = EnvOverviewStore.getTpEnvId;
    const {
      InstancesStore: { reStarts, loadInstanceAll },
    } = this.props;
    this.setState({
      confirmLoading: true,
    });
    reStarts(projectId, id)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          const time = Date.now();
          loadInstanceAll(true, projectId, { envId }, time).catch(err => {
            InstancesStore.changeLoading(false);
            Choerodon.handleResponseError(err);
          });
          this.closeConfirm();
        }
        this.setState({
          confirmLoading: false,
        });
      })
      .catch(err => {
        InstancesStore.changeLoading(false);
        this.setState({
          confirmLoading: false,
        });
        Choerodon.handleResponseError(err);
      });
  };

  /**
   * 升级配置实例信息
   */
  upgradeIst = async (record) => {
    const { InstancesStore, intl } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const { code, id, envId, commandVersionId, appId } = record;

    InstancesStore.setValue(null);
    try {
      // 升级失败仍要传入 commandVersionId，勿修改
      // 升级失败，但是新版本的逻辑已经存在于后端
      const update = await InstancesStore.loadUpVersion(projectId, commandVersionId);
      const result = handleProptError(update);
      if (result) {
        if (result.length === 0) {
          Choerodon.prompt(intl.formatMessage({ id: 'ist.noUpVer' }));
        } else {
          const idArr = {
            appId,
            environmentId: envId,
            appVersionId: result[0].id,
          };
          this.setState({ idArr, id, name: code });
          const res = await InstancesStore.loadValue(projectId, id, result[0].id);
          if (res) {
            this.setState({ upgradeVisible: true });
          }
        }
      }
    } catch (e) {
      InstancesStore.changeLoading(false);
      Choerodon.handleResponseError(e);
    }
  };

  /**
   * 修改&升级配置信息侧边栏
   * @param res 是否重载数据
   */
  closeConfigSidebar = res => {
    const {
      InstancesStore: {
        getAppId,
      },
    } = this.props;

    this.setState({
      changeVisible: false,
      upgradeVisible: false,
    });

    res && this.reloadData(res, !res, getAppId);
  };

  /**
   * 页面数据重载
   * @param spin 加载动画
   * @param clear 清空筛选条件
   * @param appId
   */
  reloadData = (spin, clear, appId = false) => {
    const { id: projectId } = AppState.currentMenuType;
    const { InstancesStore } = this.props;

    const envId = EnvOverviewStore.getTpEnvId;
    clear && InstancesStore.setIstTableFilter(null);

    const time = Date.now();
    InstancesStore.loadInstanceAll(spin, projectId, { envId, appId }, time).catch(
      err => {
        InstancesStore.changeLoading(false);
        Choerodon.handleResponseError(err);
      },
    );
  };

  /**
   * 点击刷新
   * @param spin 是否出现加载动画
   * @param clear 是否清空筛选条件
   */
  reload = (spin = true, clear = false) => {
    const { id: projectId } = AppState.currentMenuType;
    const {
      InstancesStore: {
        getAppPageSize,
        loadAppNameByEnv,
        getAppPage,
        getAppId,
      },
    } = this.props;
    const envId = EnvOverviewStore.getTpEnvId;

    loadAppNameByEnv(projectId, envId, getAppPage - 1, getAppPageSize);

    this.reloadData(spin, clear, getAppId);
  };

  /**
   * 删除实例
   */
  handleDelete = id => {
    const { id: projectId } = AppState.currentMenuType;

    const { InstancesStore } = this.props;

    const { loadInstanceAll, deleteInstance, getAppId } = InstancesStore;
    const envId = EnvOverviewStore.getTpEnvId;

    this.setState({
      deleteLoading: true,
    });
    deleteInstance(projectId, id)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          InstancesStore.setIstTableFilter(null);
          InstancesStore.setIstPage(null);
          const time = Date.now();
          loadInstanceAll(true, projectId, { envId, getAppId }, time).catch(err => {
            InstancesStore.changeLoading(false);
            Choerodon.handleResponseError(err);
          });
        }

        this.setState({
          openRemove: false,
          deleteLoading: false,
        });
      })
      .catch(error => {

        this.setState({
          deleteLoading: false,
        });
        Choerodon.handleResponseError(error);
      });
    InstancesStore.setIstTableFilter(null);
  };

  /**
   * 关闭删除数据的模态框
   */
  closeDeleteModal(id) {
    this.setState({
      openRemove: false,
    });
  }

  /**
   * 启停用实例
   * @param id 实例ID
   * @param status 状态
   */
  activeIst = (id, status) => {
    const { id: projectId } = AppState.currentMenuType;
    const {
      InstancesStore: { changeIstActive, loadInstanceAll },
    } = this.props;
    const envId = EnvOverviewStore.getTpEnvId;
    this.setState({
      confirmLoading: true,
    });

    if (status === 'stop') {
      InstancesStore.setTargetCount({});
    }

    changeIstActive(projectId, id, status).then(data => {
      const res = handleProptError(data);
      if (res) {
        InstancesStore.setAppId(null);
        InstancesStore.setIstTableFilter(null);
        const time = Date.now();
        loadInstanceAll(true, projectId, { envId }, time).catch(err => {
          InstancesStore.changeLoading(false);
          Choerodon.handleResponseError(err);
        });
        this.closeConfirm();
      }
      this.setState({
        confirmLoading: false,
      });
    });
  };

  /**
   * 打开确认框
   * @param record
   * @param type 类型：重新部署或启停实例
   */
  openConfirm = (record, type) => {
    const { id, code } = record;
    this.setState({
      confirmType: type,
      id,
      name: code,
    });
  };

  /**
   * 关闭确认框
   */
  closeConfirm = () => {
    this.setState({
      confirmType: '',
    });
  };

  /**
   * action 权限控制
   * @param record 行数据
   * @returns {*}
   */
  columnAction(record) {
    const { id: projectId, type, organizationId } = AppState.currentMenuType;
    const {
      intl: { formatMessage },
    } = this.props;
    const { status, connect, appVersionId } = record;
    const actionType = {
      detail: {
        service: ['devops-service.application-instance.listResources'],
        text: formatMessage({ id: 'ist.detail' }),
        action: this.linkDeployDetail.bind(this, record),
      },
      change: {
        service: ['devops-service.application-instance.queryValues'],
        text: formatMessage({ id: 'ist.values' }),
        action: this.updateConfig.bind(this, record),
      },
      restart: {
        service: ['devops-service.application-instance.restart'],
        text: formatMessage({ id: 'ist.reDeploy' }),
        action: this.openConfirm.bind(this, record, 'reDeploy'),
      },
      update: {
        service: ['devops-service.application-version.getUpgradeAppVersion'],
        text: formatMessage({ id: 'ist.upgrade' }),
        action: this.upgradeIst.bind(this, record),
      },
      stop: {
        service: [
          'devops-service.application-instance.start',
          'devops-service.application-instance.stop',
        ],
        text:
          status !== 'stopped'
            ? formatMessage({ id: 'ist.stop' })
            : formatMessage({ id: 'ist.run' }),
        action:
          status !== 'stopped'
            ? this.openConfirm.bind(this, record, 'stop')
            : this.openConfirm.bind(this, record, 'start'),
      },
      delete: {
        service: ['devops-service.application-instance.delete'],
        text: formatMessage({ id: 'ist.del' }),
        action: this.handleOpen.bind(this, record),
      },
    };
    let actionItem = [];
    switch (status) {
      case 'stopped':
        actionItem = ['detail', 'stop', 'delete'];
        break;
      case 'failed':
        actionItem = appVersionId
          ? ['detail', 'change', 'restart', 'update', 'stop', 'delete']
          : ['detail', 'change', 'restart', 'update', 'delete'];
        break;
      case 'running':
        actionItem = [
          'detail',
          'change',
          'restart',
          'update',
          'stop',
          'delete',
        ];
        break;
      default:
        actionItem = ['detail'];
    }
    if (!connect) {
      actionItem = ['detail'];
    }
    const actionData = _.map(actionItem, item => ({
      projectId,
      type,
      organizationId,
      ...actionType[item],
    }));
    return <Action data={actionData} />;
  }

  renderStatus(record) {
    const { code, status, error } = record;
    return <StatusIcon name={code} status={status || ''} error={error || ''} />;
  }

  renderAppName(record) {
    const { id: currentProject } = AppState.currentMenuType;
    const { projectId, appName } = record;
    return (
      <AppName
        width={0.18}
        name={appName}
        showIcon={!!projectId}
        self={projectId === Number(currentProject)}
      />
    );
  }

  /**
   * 打开删除数据模态框
   */
  handleOpen(record) {
    const { id, code } = record;
    this.setState({ openRemove: true, id, name: code });
  }

  render() {
    DevopsStore.initAutoRefresh('ist', this.reload);

    const { id: projectId, name: projectName } = AppState.currentMenuType;
    const {
      InstancesStore,
      intl: { formatMessage },
    } = this.props;
    const {
      getIstAll,
      getPageInfo,
      getAppNameByEnv,
      getAppPageInfo: { current, total, pageSize },
      getIsLoading,
      getIstParams: { filters, param },
      getAppId,
    } = InstancesStore;
    const {
      name,
      changeVisible,
      upgradeVisible,
      idArr,
      openRemove,
      id,
      deleteLoading,
      confirmType,
      confirmLoading,
    } = this.state;

    const envData = EnvOverviewStore.getEnvcard;
    const envId = EnvOverviewStore.getTpEnvId;

    const title = _.find(envData, ['id', envId]);

    const appNameDom = getAppNameByEnv.length ? (
      _.map(getAppNameByEnv, d => (
        <div
          role="none"
          className={`c7n-deploy-single_card ${
            Number(getAppId) === d.id ? 'c7n-deploy-single_card-active' : ''
            }`}
          onClick={this.loadDetail.bind(this, d.id)}
          key={`${d.id}-${d.projectId}`}
        >
          <AppName
            width="165px"
            name={d.name}
            showIcon={!!d.projectId}
            self={d.projectId === Number(projectId)}
          />
        </div>
      ))
    ) : (
      <div className="c7n-deploy-single_card">
        <div className="c7n-deploy-square">
          <div>App</div>
        </div>
        <FormattedMessage id="ist.noApp" />
      </div>
    );

    const columns = [
      {
        title: <FormattedMessage id="deploy.instance" />,
        key: 'code',
        filters: [],
        filteredValue: filters.code || [],
        render: this.renderStatus,
      },
      {
        title: <Tips type="title" data="deploy.ver" />,
        key: 'version',
        filters: [],
        filteredValue: filters.version || [],
        render: record => <UploadIcon dataSource={record} />,
      },
      {
        title: <FormattedMessage id="deploy.app" />,
        key: 'appName',
        filters: [],
        filteredValue: filters.appName || [],
        render: this.renderAppName,
      },
      {
        title: <FormattedMessage id="deploy.pod" />,
        key: 'podStatus',
        render: record => <PodStatus dataSource={record} />,
      },
      {
        width: 56,
        className: 'c7n-operate-icon',
        key: 'action',
        render: this.columnAction,
      },
    ];

    const detailDom = (
      <Fragment>
        <div className="c7n-deploy-env-title">
          <FormattedMessage id="deploy.app" />
        </div>
        <div>{appNameDom}</div>
        {getAppNameByEnv.length && total > 15 ? (
          <div className="c7n-store-pagination">
            <Pagination
              tiny={false}
              showSizeChanger
              showSizeChangerLabel={false}
              total={total || 0}
              current={current || 0}
              pageSize={pageSize || 0}
              onChange={this.onPageChange}
              onShowSizeChange={this.onPageChange}
            />
          </div>
        ) : null}
        <div className="c7n-deploy-env-title c7n-deploy-env-ist">
          <FormattedMessage id="ist.head" />
        </div>
        <Table
          className="c7n-expand-table"
          filterBarPlaceholder={formatMessage({ id: 'filter' })}
          onChange={this.tableChange}
          dataSource={getIstAll}
          loading={getIsLoading}
          pagination={getPageInfo}
          filters={param.slice() || []}
          columns={columns}
          rowKey={record => record.id}
          expandedRowRender={record => <ExpandRow record={record} />}
        />
      </Fragment>
    );

    return (
      <Page
        className="c7n-region"
        service={[
          'devops-service.application-instance.pageByOptions',
          'devops-service.application.pageByEnvIdAndStatus',
          'devops-service.application-instance.listResources',
          'devops-service.devops-environment.listByProjectIdAndActive',
          'devops-service.application-version.getUpgradeAppVersion',
          'devops-service.application-instance.listByAppId',
          'devops-service.application-instance.queryValues',
          'devops-service.application-instance.formatValue',
          'devops-service.application-instance.stop',
          'devops-service.application-instance.start',
          'devops-service.application-instance.deploy',
          'devops-service.application-instance.delete',
          'devops-service.application-instance.restart',
        ]}
      >
        {envData && envData.length && envId ? (
          <Fragment>
            <Header title={<FormattedMessage id="ist.head" />}>
              <Select
                className={`${
                  envId
                    ? 'c7n-header-select'
                    : 'c7n-header-select c7n-select_min100'
                  }`}
                dropdownClassName="c7n-header-env_drop"
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
              <RefreshBtn name="ist" onFresh={this.reload} />
            </Header>
            <Content className="page-content">
              <div className="c7n-instance-header">
                <div className="c7n-instance-title">
                  {formatMessage(
                    { id: 'ist.title.env' },
                    { name: title ? title.name : projectName },
                  )}
                </div>
                <div className="c7n-instance-describe">
                  {formatMessage({ id: 'ist.description' })}
                  <a href={formatMessage({ id: 'ist.link' })}>
                    {formatMessage({ id: 'learnmore' })}
                    <Icon type="open_in_new" />
                  </a>
                </div>
              </div>
              {detailDom}
              {changeVisible && (
                <ValueConfig
                  store={InstancesStore}
                  visible={changeVisible}
                  name={name}
                  id={id}
                  idArr={idArr}
                  onClose={this.closeConfigSidebar}
                />
              )}
              {upgradeVisible && (
                <UpgradeIst
                  store={InstancesStore}
                  visible={upgradeVisible}
                  name={name}
                  appInstanceId={id}
                  idArr={idArr}
                  onClose={this.closeConfigSidebar}
                />
              )}
              <Modal
                title={`${formatMessage({ id: 'ist.del' })}“${name}”`}
                visible={openRemove}
                closable={false}
                footer={[
                  <Button
                    key="back"
                    onClick={this.closeDeleteModal.bind(this, id)}
                    disabled={deleteLoading}
                  >
                    <FormattedMessage id="cancel" />
                  </Button>,
                  <Button
                    key="submit"
                    type="danger"
                    loading={deleteLoading}
                    onClick={this.handleDelete.bind(this, id)}
                  >
                    <FormattedMessage id="delete" />
                  </Button>,
                ]}
              >
                <div className="c7n-padding-top_8">
                  <FormattedMessage id="ist.delDes" />
                </div>
              </Modal>
              <Modal
                title={`${formatMessage({ id: 'ist.reDeploy' })}“${name}”`}
                visible={confirmType === 'reDeploy'}
                onOk={this.reStart.bind(this, id)}
                onCancel={this.closeConfirm}
                confirmLoading={confirmLoading}
                closable={false}
              >
                <div className="c7n-padding-top_8">
                  <FormattedMessage id="ist.reDeployDes" />
                </div>
              </Modal>
              <Modal
                title={`${formatMessage({
                  id: `${confirmType === 'stop' ? 'ist.stop' : 'ist.run'}`,
                })}“${name}”`}
                visible={confirmType === 'stop' || confirmType === 'start'}
                onOk={this.activeIst.bind(this, id, confirmType)}
                onCancel={this.closeConfirm}
                confirmLoading={confirmLoading}
                closable={false}
              >
                <div className="c7n-padding-top_8">
                  <FormattedMessage id={`ist.${confirmType}Des`} />
                </div>
              </Modal>
            </Content>
          </Fragment>
        ) : (
          <DepPipelineEmpty
            title={<FormattedMessage id="ist.head" />}
            type="env"
          />
        )}
      </Page>
    );
  }
}

export default withRouter(injectIntl(Instances));
