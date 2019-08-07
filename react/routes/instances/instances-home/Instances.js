/* eslint-disable no-bitwise, react/no-access-state-in-setstate */
import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Table, Tooltip, Pagination, Button, Icon, Modal, Spin } from 'choerodon-ui';
import { Action, stores } from '@choerodon/boot';
import _ from 'lodash';
import { handleProptError } from '../../../utils';
import ValueConfig from '../ValueConfig';
import UpgradeIst from '../UpgradeIst';
import ExpandRow from '../components/ExpandRow';
import StatusIcon from '../../../components/StatusIcon';
import UploadIcon from '../components/UploadIcon';
import AppName from '../../../components/appName';
import Tips from '../../../components/Tips/Tips';
import PodStatus from '../components/PodStatus/PodStatus';
import EnvOverviewStore from '../../envOverview/stores';
import DeleteModal from '../../../components/deleteModal';
import Networking from '../components/Networking';
import '../../main.scss';
import './index.scss';

const { AppState } = stores;
const ENV_ID = 458;
const IST_ID = 960;

@observer
class Instances extends Component {
  state = {
    upgradeVisible: false,
    changeVisible: false,
    deleteLoading: false,
    confirmLoading: false,
    confirmType: '',
    idArr: {},
    deleteArr: [],
    resourceData: {},
    resourceLoading: {},
    defaultIst: null,
  };

  componentDidMount() {
    const { InstancesStore } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const { loadResource } = InstancesStore;

    loadResource(projectId, 960);
  }

  componentWillUnmount() {
    const { InstancesStore } = this.props;
    InstancesStore.setValue(null);
  }

  /**
   * 选择应用后获取实例列表
   * @param appId
   */
  loadDetail = (appId) => {
    const { InstancesStore } = this.props;
    const currentApp = InstancesStore.getAppId;
    const nextApp = appId !== currentApp && appId;

    this.setState({ defaultIst: null });

    InstancesStore.setAppId(nextApp);
    InstancesStore.setIstTableFilter(null);
    InstancesStore.setIstPage(null);

    this.reloadData(true, true, nextApp);
  };

  /**
   * 查看部署详情
   */
  linkDeployDetail = ({ id, status, code }) => {
    const {
      InstancesStore,
      history,
      location: {
        state,
      },
    } = this.props;
    InstancesStore.setIsCache(true);

    const {
      id: projectId,
      name: projectName,
      type,
      organizationId,
    } = AppState.currentMenuType;

    history.push({
      pathname: `/devops/instance/${id}/${status}/${code}/detail`,
      search: `?type=${type}&id=${projectId}&name=${encodeURIComponent(
        projectName,
      )}&organizationId=${organizationId}`,
      state,
    });
  };

  /**
   * 查询应用标签及实例列表
   * @param id 环境id
   */
  handleEnvSelect = (id) => {
    const { id: projectId } = AppState.currentMenuType;
    const { InstancesStore } = this.props;
    const { loadAppNameByEnv, getAppPage, getAppPageSize } = InstancesStore;

    EnvOverviewStore.setTpEnvId(id);
    InstancesStore.setAppId(null);

    this.setState({ defaultIst: null });

    loadAppNameByEnv(projectId, id, getAppPage, getAppPageSize);
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

    this.setState({ defaultIst: null });

    InstancesStore.setIstTableFilter({ filters, param });
    InstancesStore.setIstPage({ page: current, pageSize });
    InstancesStore.loadInstanceAll(true, projectId, {
      envId,
      appId,
    }, time).catch((err) => {
      InstancesStore.changeLoading(false);
      Choerodon.handleResponseError(err);
    });
  };

  /**
   * 修改配置实例信息
   */
  updateConfig = async ({ code, id, envId, commandVersionId, appId }) => {
    const { InstancesStore } = this.props;
    const { id: projectId } = AppState.currentMenuType;

    this.setState({
      idArr: {
        environmentId: envId,
        appVersionId: commandVersionId,
        appId,
      },
      name: code,
    });

    InstancesStore.setValue(null);

    const res = await InstancesStore.loadValue(projectId, id, commandVersionId);
    if (res) {
      this.setState({ changeVisible: true, id });
    }
  };

  /**
   * 重新部署
   * @param id
   */
  reStart = async (id) => {
    const { id: projectId } = AppState.currentMenuType;
    const {
      InstancesStore,
    } = this.props;
    const { defaultIst } = this.state;
    const {
      reStarts,
      loadInstanceAll,
      changeLoading,
      getAppId,
    } = InstancesStore;

    const envId = EnvOverviewStore.getTpEnvId;

    this.setState({ confirmLoading: true });

    const response = await reStarts(projectId, id)
      .catch((err) => {
        changeLoading(false);
        this.setState({ confirmLoading: false });
        Choerodon.handleResponseError(err);
      });
    const result = handleProptError(response);
    if (result) {
      const time = Date.now();

      loadInstanceAll(true, projectId, { envId, getAppId, instanceId: defaultIst }, time)
        .catch((err) => {
          InstancesStore.changeLoading(false);

          Choerodon.handleResponseError(err);
        });
    }
    this.closeConfirm();
    this.setState({ confirmLoading: false });
  };

  /**
   * 升级配置实例信息
   */
  upgradeIst = async (record) => {
    const { InstancesStore } = this.props;
    const { code, id, envId, appId } = record;
    InstancesStore.setValue(null);
    this.setState({
      upgradeVisible: true,
      idArr: {
        environmentId: envId,
        appId,
      },
      id,
      name: code,
    });
  };

  /**
   * 修改&升级配置信息侧边栏
   * @param res 是否重载数据
   */
  closeConfigSidebar = (res) => {
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
    const { defaultIst } = this.state;

    const envId = EnvOverviewStore.getTpEnvId;
    clear && InstancesStore.setIstTableFilter(null);

    const time = Date.now();
    InstancesStore.loadInstanceAll(spin, projectId, { envId, appId, instanceId: defaultIst }, time)
      .catch(
        (err) => {
          InstancesStore.changeLoading(false);
          Choerodon.handleResponseError(err);
        },
      );
  };

  /**
   * 删除实例
   * @param id
   * @param callback 当删除请求报错后的处理，用于清除定时器和loading状态
   * @returns {Promise<void>}
   */
  handleDelete = async (id, callback) => {
    const { id: projectId } = AppState.currentMenuType;
    const { InstancesStore } = this.props;
    const { loadInstanceAll, deleteInstance, getAppId } = InstancesStore;
    const envId = EnvOverviewStore.getTpEnvId;
    const { defaultIst } = this.state;

    this.setState({ deleteLoading: true, defaultIst: null });

    const response = await deleteInstance(projectId, id)
      .catch((error) => {
        this.setState({ deleteLoading: false });

        callback && callback();

        Choerodon.handleResponseError(error);
      });

    const res = handleProptError(response);
    if (res) {
      this.removeDeleteModal(id);

      InstancesStore.setIstTableFilter(null);
      InstancesStore.setIstPage(null);
      loadInstanceAll(true, projectId, { envId, getAppId, instanceId: defaultIst }, Date.now())

        .catch((err) => {
          InstancesStore.changeLoading(false);
          Choerodon.handleResponseError(err);
        });
    }

    this.setState({
      deleteLoading: false,
    });
    InstancesStore.setIstTableFilter(null);
  };

  /**
   * 打开删除数据模态框
   * NOTE: 删除模态框中存在定时器，只有在发出删除请求后，当前的模态框才能从Dom中移除，所以使用一个数组保存所有的删除模态框
   */
  openDeleteModal({ id, code }) {
    const deleteArr = [...this.state.deleteArr];

    const currentIndex = _.findIndex(deleteArr, item => id === item.deleteId);

    if (~currentIndex) {
      const newItem = {
        ...deleteArr[currentIndex],
        display: true,
      };
      deleteArr.splice(currentIndex, 1, newItem);
    } else {
      deleteArr.push({
        display: true,
        deleteId: id,
        name: code,
      });
    }

    this.setState({ deleteArr });
  }

  /**
   * 关闭删除数据的模态框
   */
  closeDeleteModal = (id) => {
    const deleteArr = [...this.state.deleteArr];

    const current = _.find(deleteArr, item => id === item.deleteId);

    current.display = false;

    this.setState({ deleteArr });
  };

  /**
   * 从当前模态框列表中移除已经完成的删除模态框
   * @param id
   */
  removeDeleteModal(id) {
    const { deleteArr } = this.state;
    const newDeleteArr = _.filter(deleteArr, ({ deleteId }) => deleteId !== id);
    this.setState({ deleteArr: newDeleteArr });
  }

  /**
   * 启停用实例
   * @param id 实例ID
   * @param status 状态
   */
  activeIst = (id, status) => {
    const { id: projectId } = AppState.currentMenuType;
    const { InstancesStore } = this.props;
    const envId = EnvOverviewStore.getTpEnvId;
    const { defaultIst } = this.state;
    const {
      changeIstActive,
      loadInstanceAll,
    } = InstancesStore;

    this.setState({
      confirmLoading: true,
    });

    if (status === 'stop') {
      InstancesStore.setTargetCount({});
    }

    changeIstActive(projectId, id, status).then((data) => {
      const res = handleProptError(data);
      if (res) {
        InstancesStore.setAppId(null);
        InstancesStore.setIstTableFilter(null);
        const time = Date.now();

        loadInstanceAll(true, projectId, { envId, instanceId: defaultIst }, time).catch((err) => {
          InstancesStore.changeLoading(false);

          Choerodon.handleResponseError(err);
        });
        this.closeConfirm();
      }
      this.setState({
        confirmLoading: false,
      });
    }).catch((e) => {
      this.setState({
        confirmLoading: false,
      });
      Choerodon.handleResponseError(e);
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
      id: null,
      name: null,
    });
  };

  /**
   * action 权限控制
   * @param record 行数据
   * @returns {*}
   */
  columnAction = (record) => {
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
        text: formatMessage({ id: 'ist.change' }),
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
        text: formatMessage({ id: 'ist.delete' }),
        action: this.openDeleteModal.bind(this, record),
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
  };

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

  renderNetworking = (record) => {
    const { serviceCount, ingressCount } = record;
    const { intl: { formatMessage } } = this.props;
    return (
      <div>
        <FormattedMessage
          id="ist.networking.info"
          values={{ serviceCount, ingressCount }}
        />
        <Tooltip title={formatMessage({ id: 'ist.networking.header' })}>
          <Button
            icon="open_in_new"
            shape="circle"
            onClick={this.openConfirm.bind(this, record, 'networking')}
          />
        </Tooltip>
      </div>
    );
  };

  ExpandChange = (expend, { id }) => {
    const {
      InstancesStore,
    } = this.props;
    const { projectId } = AppState.currentMenuType;
    const {
      resourceData,
      resourceLoading,
    } = this.state;
    if (expend) {
      if (!resourceData[id]) {
        this.setState({ resourceLoading: _.assign({}, resourceLoading, { [id]: true }) });
      }
      InstancesStore.loadResource(projectId, id)
        .then((data) => {
          if (!resourceData[id]) {
            this.setState({ resourceLoading: _.assign({}, resourceLoading, { [id]: false }) });
          }
          if (data && !data.failed) {
            if (resourceData[id] && _.isEqual(data, resourceData[id])) {
              return;
            }
            this.setState({ resourceData: _.assign({}, resourceData, { [id]: data }) });
          }
        });
    }
  };

  render() {
    const {
      InstancesStore,
      intl: { formatMessage },
    } = this.props;
    const {
      getIstAll,
      getPageInfo,
      getIsLoading,
      getIstParams: { filters, param },
    } = InstancesStore;
    const {
      name,
      changeVisible,
      upgradeVisible,
      idArr,
      id,
      confirmType,
      confirmLoading,
      deleteLoading,
      deleteArr,
      resourceLoading,
      resourceData,
    } = this.state;

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
        title: 'Networking',
        key: 'networking',
        render: this.renderNetworking,
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

    const deleteModals = _.map(deleteArr, ({ name: modalName, display, deleteId }) => (<DeleteModal
      key={deleteId}
      title={`${formatMessage({ id: 'ist.delete' })}“${modalName}”`}
      visible={display}
      objectId={deleteId}
      loading={deleteLoading}
      objectType="instance"
      onClose={this.closeDeleteModal}
      onOk={this.handleDelete}
    />));

    return (
      <Fragment>
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
          onExpand={this.ExpandChange}
          expandedRowRender={record => (
            resourceLoading[record.id]
              ? <Spin spinning className="c7n-ist-expandrow-loading" />
              : <ExpandRow record={Object.assign({}, record, resourceData[record.id] || {})} />
          )}
        />
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
        {deleteModals}
      </Fragment>
    );
  }
}

export default withRouter(injectIntl(Instances));
