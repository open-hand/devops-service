/* eslint-disable react/sort-comp, react/no-access-state-in-setstate, no-bitwise */
import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { observable, action } from 'mobx';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Form,
  Collapse,
  Icon,
  Input,
  Tooltip,
  Modal,
  Progress,
  Button,
  Spin,
} from 'choerodon-ui';
import { Action } from '@choerodon/boot';
import _ from 'lodash';
import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/base16-dark.css';
import InstancesStore from '../../../../stores/project/instances/InstancesStore';
import DomainStore from '../../../../stores/project/domain';
import NetworkConfigStore from '../../../../stores/project/networkConfig';
import ValueConfig from '../../instances/ValueConfig';
import UpgradeIst from '../../instances/UpgradeIst';
import CreateDomain from '../../domain/createDomain';
import CreateNetwork from '../../networkConfig/createNetwork';
import LoadingBar from '../../../../components/loadingBar';
import ExpandRow from '../../instances/components/ExpandRow';
import UploadIcon from '../../instances/components/UploadIcon';
import PodStatus from '../../instances/components/PodStatus/PodStatus';
import DeleteModal from '../../../../components/deleteModal';
import Networking from '../../instances/components/Networking';
import { handleProptError } from '../../../../utils';

import '../EnvOverview.scss';
import '../../instances/Instances.scss';
import '../../../main.scss';

const Panel = Collapse.Panel;

@Form.create({})
@withRouter
@injectIntl
@inject('AppState')
@observer
export default class AppOverview extends Component {
  @observable pageSize = 10;

  @observable visible = false;

  @observable visibleUp = false;

  @observable page = 1;

  @observable loading = false;

  @observable ist = {};

  @observable idArr = {};

  @observable name = '';

  @observable showSide = false;

  @observable containerName = '';

  @observable containerArr = [];

  @observable podName = '';

  @observable activeKey = [];

  @observable showDomain = false;

  @observable showNetwork = false;

  @observable domainId = null;

  @observable appId = null;

  @observable istId = null;

  @observable appName = '';

  @observable domainType = '';

  @observable domainTitle = '';

  @observable istName = '';

  @observable confirmType = '';

  @observable confirmLoading = false;

  state = {
    deleteArr: [],
    deleteLoading: false,
    resourceData: {},
    resourceLoading: {},
  };

  componentDidMount() {
    const { store } = this.props;
    const refresh = store.getRefresh;
    if (refresh) {
      this.emitEmpty();
    }
  }

  componentWillMount() {
    InstancesStore.setValue(null);
  }

  /**
   * 搜索函数
   */
  onSearch = () => {
    this.searchInput.focus();
    const {
      store,
      envId,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const val = store.getVal;
    const searchParam = {};
    const postData = {
      searchParam,
      param: val.toString(),
    };
    store.loadIstOverview(true, projectId, envId, postData);
  };

  /**
   * 搜索输入赋值
   * @param e
   */
  @action
  onChangeSearch = (e) => {
    const { store } = this.props;
    store.setVal(e.target.value);
  };

  @action
  onChange = (e) => {
    this.activeKey = e;

    const {
      AppState: {
        currentMenuType: {
          projectId,
        },
      },
    } = this.props;
    const {
      resourceData,
      resourceLoading,
    } = this.state;
    if (e) {
      if (!resourceData[e]) {
        this.setState({ resourceLoading: _.assign({}, resourceLoading, { [e]: true }) });
      }
      InstancesStore.loadResource(projectId, e)
        .then((data) => {
          if (!resourceData[e]) {
            this.setState({ resourceLoading: _.assign({}, resourceLoading, { [e]: false }) });
          }
          if (data && !data.failed) {
            if (resourceData[e] && _.isEqual(data, resourceData[e])) {
              return;
            }
            this.setState({ resourceData: _.assign({}, resourceData, { [e]: data }) });
          }
        });
    }
  };

  /**
   * 加载实例总览列表
   */
  loadIstOverview = (spin = true) => {
    const {
      store,
      envId,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    store.loadIstOverview(spin, projectId, envId);
  };

  /**
   * 查看部署详情
   */
  linkDeployDetail = ({ id, status, code }) => {
    const {
      history,
      AppState: {
        currentMenuType: {
          id: projectId,
          name: projectName,
          type,
          organizationId,
        },
      },
    } = this.props;

    history.push({
      pathname: `/devops/instance/${id}/${status}/${code}/detail`,
      search: `?type=${type}&id=${projectId}&name=${encodeURIComponent(
        projectName,
      )}&organizationId=${organizationId}&overview`,
    });
  };

  getPanelHeader(data) {
    const { intl: { formatMessage } } = this.props;
    const {
      status,
      code,
      error,
      serviceCount,
      ingressCount,
    } = data;

    let nameNode = null;
    switch (status) {
      case 'running':
      case 'stopped':
        nameNode = <span className="c7n-deploy-istCode">{code}</span>;
        break;
      case 'operating':
        nameNode = <div>
          <span className="c7n-deploy-istCode">{code}</span>
          <Tooltip title={formatMessage({ id: `ist_${status}` })}>
            <Progress
              type="loading"
              size="small"
              width={15}
            />
          </Tooltip>
        </div>;
        break;
      default:
        nameNode = <div>
          <span className="c7n-deploy-istCode">{code}</span>
          <Tooltip title={`${status}: ${error || ''}`}>
            <i className="icon icon-error c7n-deploy-ist-operate" />
          </Tooltip>
        </div>;
    }

    return (
      <div className="c7n-envow-ist-header-wrap">
        <Icon type="navigate_next" />
        <div className="c7n-envow-ist-name">
          {nameNode}
        </div>
        <span className="c7n-envow-ist-version">
          <span className="c7n-envow-version-text">
            <FormattedMessage id="app.appVersion" />
            :&nbsp;&nbsp;
          </span>
          <UploadIcon dataSource={data} />
        </span>
        <div className="c7n-appow-networking" onClick={this.handlerAction}>
          <span className="c7n-envow-version-text">
            Networking:&nbsp;&nbsp;
          </span>
          <FormattedMessage
            id="ist.networking.info"
            values={{ serviceCount, ingressCount }}
          />
          <Tooltip title={formatMessage({ id: 'ist.networking.header' })}>
            <Button
              icon="open_in_new"
              shape="circle"
              onClick={this.openConfirm.bind(this, data, 'networking')}
            />
          </Tooltip>
        </div>
        <div className="c7n-appow-pod-status">
          <PodStatus dataSource={data} />
        </div>
        <div className="c7n-envow-ist-action">{this.columnAction(data)}</div>
      </div>
    );
  }

  /**
   * 重新部署
   * @param id
   */
  reStart = (id) => {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    this.confirmLoading = true;
    InstancesStore.reStarts(projectId, id).then((error) => {
      if (error && error.failed) {
        Choerodon.prompt(error.message);
      } else {
        this.loadIstOverview();
        this.closeConfirm();
      }
      this.confirmLoading = false;
    }).catch((e) => {
      this.confirmLoading = false;
      Choerodon.handleResponseError(e);
    });
  };

  /**
   * 修改配置实例信息
   */
  @action
  updateConfig = async ({ code, id, envId, commandVersionId, appId }) => {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    this.id = id;
    this.name = code;
    this.idArr = {
      environmentId: envId,
      appVersionId: commandVersionId,
      appId,
    };
    InstancesStore.setValue(null);
    const result = await InstancesStore.loadValue(projectId, id, commandVersionId);
    if (result) {
      this.visible = true;
    }
  };

  /**
   * 升级配置实例信息
   */
  @action
  upgradeIst = async (record) => {
    const { code, id, envId, appId } = record;

    InstancesStore.setValue(null);
    this.visibleUp = true;
    this.id = id;
    this.name = code;
    this.idArr = {
      environmentId: envId,
      appId,
    };
  };

  /**
   * 打开确认框
   * @param record
   * @param type
   */
  @action
  openConfirm = (record, type) => {
    const { id, code, appId } = record;
    this.confirmType = type;
    this.id = id;
    this.istName = code;
    this.appId = appId;
  };

  /**
   * 关闭确认框
   */
  @action
  closeConfirm = () => {
    this.confirmType = '';
  };

  /**
   * 启停用实例
   * @param id 实例ID
   * @param status 状态
   */
  activeIst = (id, status) => {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    this.confirmLoading = true;
    InstancesStore.changeIstActive(projectId, id, status).then((error) => {
      if (error && error.failed) {
        Choerodon.prompt(error.message);
      } else {
        this.loadIstOverview();
        this.closeConfirm();
      }
      this.confirmLoading = false;
    }).catch((e) => {
      this.confirmLoading = false;
      Choerodon.handleResponseError(e);
    });
  };

  /**
   * 关闭网络侧边栏
   */
  @action
  closeNetwork = (isLoad) => {
    const { store } = this.props;
    this.props.form.resetFields();
    this.showNetwork = false;
    if (isLoad) {
      this.loadIstOverview();
      store.setTabKey('network');
    }
  };

  /**
   * 关闭域名侧边栏
   */
  @action
  closeDomain = () => {
    this.props.form.resetFields();
    this.showDomain = false;
    this.domainId = null;
    this.loadIstOverview();
  };

  /**
   * 关闭滑块
   * @param res 是否重新部署需要重载数据
   */
  @action
  handleCancel = (res) => {
    this.visible = false;
    if (res) {
      this.loadIstOverview();
    }
  };

  /**
   * 关闭升级滑块
   * @param res 是否重新部署需要重载数据
   */
  @action
  handleCancelUp = (res) => {
    this.visibleUp = false;
    if (res) {
      this.loadIstOverview();
    }
  };

  handleDelete = async (id, callback) => {
    const {
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;

    this.setState({ deleteLoading: true });

    const response = await InstancesStore.deleteInstance(projectId, id)
      .catch((error) => {
        this.setState({ deleteLoading: false });
        callback && callback();
        Choerodon.handleResponseError(error);
      });

    const result = handleProptError(response);

    if (result) {
      this.removeDeleteModal(id);
      this.loadIstOverview();
    }

    this.setState({ deleteLoading: false });
  };

  openDeleteModal(id, name) {
    const deleteArr = [...this.state.deleteArr];

    const currentIndex = _.findIndex(deleteArr, item => id === item.deleteId);

    if (~currentIndex) {
      const newItem = {
        ...deleteArr[currentIndex],
        display: true,
      };
      deleteArr.splice(currentIndex, 1, newItem);
    } else {
      const newItem = {
        display: true,
        deleteId: id,
        name,
      };
      deleteArr.push(newItem);
    }

    this.setState({ deleteArr });
  }

  closeDeleteModal = (id) => {
    const deleteArr = [...this.state.deleteArr];

    const current = _.find(deleteArr, item => id === item.deleteId);

    current.display = false;

    this.setState({ deleteArr });
  };

  removeDeleteModal(id) {
    const { deleteArr } = this.state;
    const newDeleteArr = _.filter(deleteArr, ({ deleteId }) => deleteId !== id);
    this.setState({ deleteArr: newDeleteArr });
  }

  /**
   *打开域名创建弹框
   */
  @action
  createDomain = (type, id = '') => {
    this.props.form.resetFields();
    if (type === 'create') {
      this.domainTitle = this.props.intl.formatMessage({
        id: 'domain.header.create',
      });
      this.domainType = type;
      this.domainId = id;
    } else {
      this.domainTitle = this.props.intl.formatMessage({
        id: 'domain.header.update',
      });
      this.domainType = type;
      this.domainId = id;
    }
    this.showDomain = true;
  };

  /**
   * 打开创建网络
   */
  @action
  createNetwork = (appId = null, istId = null, appCode = '') => {
    this.appId = appId;
    this.istId = istId;
    this.appCode = appCode;
    this.showNetwork = true;
  };

  /**
   * 删除搜索
   */
  @action
  emitEmpty = () => {
    const { store } = this.props;
    store.setVal('');
    this.onSearch();
  };

  /**
   * 处理返回panel Dom
   * @returns {*}
   */
  panelDom = () => {
    const {
      store,
      AppState: {
        currentMenuType: {
          id: currentProjectId,
        },
      },
    } = this.props;
    const {
      resourceData,
      resourceLoading,
    } = this.state;
    const ist = store.getIst;
    if (ist) {
      if (ist.devopsEnvPreviewAppDTOS && ist.devopsEnvPreviewAppDTOS.length) {
        return _.map(ist.devopsEnvPreviewAppDTOS, (previewApp) => {
          const { appName, applicationInstanceDTOS, projectId } = previewApp;
          return (
            <div className="c7n-envow-app-wrap" key={appName}>
              <div className="c7n-envow-app">
                <Icon
                  type={
                    projectId === parseInt(currentProjectId, 10)
                      ? 'project'
                      : 'apps'
                  }
                />
                <span className="c7n-envow-app-name">{appName}</span>
              </div>
              <Collapse
                accordion
                key={`${appName}-collapse`}
                onChange={this.onChange}
              >
                {_.map(applicationInstanceDTOS, detail => (
                  <Panel
                    forceRender
                    showArrow={false}
                    header={this.getPanelHeader(detail)}
                    key={detail.id}
                  >
                    {resourceLoading[detail.id]
                      ? <Spin spinning className="c7n-ist-expandrow-loading" />
                      : <ExpandRow record={Object.assign({}, detail, resourceData[detail.id] || {})} />
                    }
                  </Panel>
                ))}
                {/* 处理Safari浏览器下，折叠面板渲染最后一个节点panel卡顿问题 */}
                <Panel
                  className="c7n-envow-none"
                  forceRender
                  key={`${appName}-none`}
                >
                  none
                </Panel>
              </Collapse>
            </div>
          );
        });
      }
      return (
        <span className="c7n-none-des">
          <FormattedMessage id="envoverview.unlist" />
        </span>
      );
    }
    return null;
  };

  /**
   * 阻止Action组件冒泡弹出折叠面板
   * @param e
   */
  handlerAction = (e) => {
    e.stopPropagation();
  };

  /**
   * action 权限控制
   * @param record 行数据
   * @returns {*}
   */
  columnAction = (record) => {
    const {
      intl: { formatMessage },
      AppState: {
        currentMenuType: {
          id: projectId,
          type,
          organizationId,
        },
      },
    } = this.props;

    const { id, code, status, connect, appVersionId } = record;

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
        action: this.openDeleteModal.bind(this, id, code),
      },
    };
    let actionItem = [];
    switch (status) {
      case 'operating' || !connect:
        actionItem = ['detail'];
        break;
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
    return <Action onClick={this.handlerAction} data={actionData} />;
  };

  render() {
    const {
      intl: { formatMessage },
      store,
    } = this.props;

    const {
      deleteArr,
      deleteLoading,
    } = this.state;

    const val = store.getVal;
    const prefix = <Icon type="search" onClick={this.onSearch} />;
    const suffix = val ? <Icon type="close" onClick={this.emitEmpty} /> : null;

    const deleteModals = _.map(deleteArr, ({ name, display, deleteId }) => (<DeleteModal
      key={deleteId}
      title={`${formatMessage({ id: 'ist.delete' })}“${name}”`}
      visible={display}
      objectId={deleteId}
      loading={deleteLoading}
      objectType="instance"
      onClose={this.closeDeleteModal}
      onOk={this.handleDelete}
    />));

    return (
      <div>
        {store.isLoading ? (
          <LoadingBar display />
        ) : (
          <React.Fragment>
            <div className="c7n-envow-search">
              <Input
                placeholder={formatMessage({ id: 'envoverview.search' })}
                value={val}
                prefix={prefix}
                suffix={suffix}
                onChange={this.onChangeSearch}
                onPressEnter={this.onSearch}
                // eslint-disable-next-line no-return-assign
                ref={node => (this.searchInput = node)}
              />
            </div>
            {this.panelDom()}
            {this.visible && (
              <ValueConfig
                store={InstancesStore}
                visible={this.visible}
                name={this.name}
                id={this.id}
                idArr={this.idArr}
                onClose={this.handleCancel}
              />
            )}
            {this.visibleUp && (
              <UpgradeIst
                store={InstancesStore}
                visible={this.visibleUp}
                name={this.name}
                appInstanceId={this.id}
                idArr={this.idArr}
                onClose={this.handleCancelUp}
              />
            )}
            {deleteModals}
            <Modal
              title={`${formatMessage({ id: 'ist.reDeploy' })}“${
                this.istName
              }”`}
              visible={this.confirmType === 'reDeploy'}
              onOk={this.reStart.bind(this, this.id)}
              onCancel={this.closeConfirm}
              confirmLoading={this.confirmLoading}
              closable={false}
            >
              <div className="c7n-padding-top_8">
                <FormattedMessage id="ist.reDeployDes" />
              </div>
            </Modal>
            <Modal
              title={`${formatMessage({
                id: `${this.confirmType === 'stop' ? 'ist.stop' : 'ist.run'}`,
              })}“${this.istName}”`}
              visible={
                this.confirmType === 'stop' || this.confirmType === 'start'
              }
              onOk={this.activeIst.bind(this, this.id, this.confirmType)}
              onCancel={this.closeConfirm}
              confirmLoading={this.confirmLoading}
              closable={false}
            >
              <div className="c7n-padding-top_8">
                <FormattedMessage id={`ist.${this.confirmType}Des`} />
              </div>
            </Modal>
            {this.showDomain && (
              <CreateDomain
                id={this.domainId}
                envId={this.props.envId}
                title={this.domainTitle}
                visible={this.showDomain}
                type={this.domainType}
                store={DomainStore}
                onClose={this.closeDomain}
              />
            )}
            {this.showNetwork && (
              <CreateNetwork
                visible={this.showNetwork}
                envId={this.props.envId}
                appId={this.appId}
                appCode={this.appCode}
                istId={this.istId}
                store={NetworkConfigStore}
                onClose={this.closeNetwork}
              />
            )}
            {this.confirmType === 'networking' && (
              <Networking
                id={this.id}
                appId={this.appId}
                name={this.istName}
                show={this.confirmType === 'networking'}
                store={InstancesStore}
                onClose={this.closeConfirm}
              />
            )}
          </React.Fragment>
        )}
      </div>
    );
  }
}
