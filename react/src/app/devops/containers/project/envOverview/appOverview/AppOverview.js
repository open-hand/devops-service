/* eslint-disable react/sort-comp */
import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { observable, action } from 'mobx';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Button,
  Form,
  Collapse,
  Icon,
  Input,
  Tooltip,
  Modal,
  Progress,
  Select,
} from 'choerodon-ui';
import { Permission, Content, Action, stores } from '@choerodon/boot';
import _ from 'lodash';
import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/base16-dark.css';
import ValueConfig from '../../instances/ValueConfig';
import UpgradeIst from '../../instances/UpgradeIst';
import '../EnvOverview.scss';
import '../../instances/Instances.scss';
import '../../../main.scss';
import InstancesStore from '../../../../stores/project/instances/InstancesStore';
import DomainStore from '../../../../stores/project/domain';
import CreateDomain from '../../domain/createDomain';
import CreateNetwork from '../../networkConfig/createNetwork';
import NetworkConfigStore from '../../../../stores/project/networkConfig';
import LoadingBar from '../../../../components/loadingBar';
import ExpandRow from '../../instances/components/ExpandRow';
import UploadIcon from '../../instances/components/UploadIcon';
import PodStatus from '../../instances/components/PodStatus/PodStatus';
import { handleProptError } from '../../../../utils';

const { AppState } = stores;
const Option = Select.Option;
const Panel = Collapse.Panel;

@observer
class AppOverview extends Component {
  @observable pageSize = 10;

  @observable visible = false;

  @observable visibleUp = false;

  @observable openRemove = false;

  @observable page = 0;

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
    const { store, envId } = this.props;
    const projectId = AppState.currentMenuType.id;
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
  onChangeSearch = e => {
    const { store } = this.props;
    store.setVal(e.target.value);
  };

  @action
  onChange = e => {
    this.activeKey = e;
  };

  /**
   * 处理页面跳转
   * @param url 跳转地址
   */
  linkToChange = url => {
    const { history } = this.props;
    history.push(url);
  };

  /**
   * 加载实例总览列表
   */
  loadIstOverview = (spin = true) => {
    const { store, envId } = this.props;
    const projectId = AppState.currentMenuType.id;
    store.loadIstOverview(spin, projectId, envId);
  };

  /**
   * 查看部署详情
   */
  linkDeployDetail = record => {
    const { id, status, code } = record;
    const { history } = this.props;
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
      )}&organizationId=${organizationId}&overview`,
    });
  };

  getPanelHeader(data) {
    const { intl } = this.props;
    const {
      status,
      code,
      error,
    } = data;

    return (
      <div className="c7n-envow-ist-header-wrap">
        <Icon type="navigate_next" />
        <div className="c7n-envow-ist-name">
          {status === 'running' || status === 'stopped' ? (
            <span className="c7n-deploy-istCode">{code}</span>
          ) : (
            <div className="c7n-envow-ist-fail">
              {status === 'operating' ? (
                <div>
                  <span className="c7n-deploy-istCode">{code}</span>
                  <Tooltip
                    title={intl.formatMessage({
                      id: `ist_${status}`,
                    })}
                  >
                    <Progress type="loading" width={15} />
                  </Tooltip>
                </div>
              ) : (
                <div>
                  <span className="c7n-deploy-istCode">{code}</span>
                  <Tooltip title={`${status}${error ? `：${error}` : ''}`}>
                    <i className="icon icon-error c7n-deploy-ist-operate" />
                  </Tooltip>
                </div>
              )}
            </div>
          )}
        </div>
        <span className="c7n-envow-ist-version">
          <span className="c7n-envow-version-text">
            <FormattedMessage id="app.appVersion" />
            :&nbsp;&nbsp;
          </span>

          <UploadIcon dataSource={data} />
        </span>
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
  reStart = id => {
    const projectId = parseInt(AppState.currentMenuType.id, 10);
    this.confirmLoading = true;
    InstancesStore.reStarts(projectId, id).then(error => {
      if (error && error.failed) {
        Choerodon.prompt(error.message);
      } else {
        this.loadIstOverview();
        this.closeConfirm();
      }
      this.confirmLoading = false;
    }).catch(e => {
      this.confirmLoading = false;
      Choerodon.handleResponseError(e);
    });
  };

  /**
   * 修改配置实例信息
   */
  @action
  updateConfig = async (record) => {
    const { code, id, envId, commandVersionId, appId } = record;
    const { id: projectId } = AppState.currentMenuType;

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
  upgradeIst = async record => {
    const { intl } = this.props;
    const { code, id, envId, commandVersionId, appId } = record;
    const { id: projectId } = AppState.currentMenuType;

    InstancesStore.setValue(null);
    try {
      const update = await InstancesStore.loadUpVersion(projectId, commandVersionId);
      const result = handleProptError(update);
      if (result) {
        if (result.length === 0) {
          Choerodon.prompt(intl.formatMessage({ id: 'ist.noUpVer' }));
        } else {
          this.id = id;
          this.name = code;
          this.idArr = {
            environmentId: envId,
            appVersionId: result[0].id,
            appId,
          };
          const res = await InstancesStore.loadValue(projectId, id, result[0].id);
          if (res) {
            this.visibleUp = true;
          }
        }
      }
    } catch (e) {
      InstancesStore.changeLoading(false);
      Choerodon.handleResponseError(e);
    }
  };

  /**
   * 打开确认框
   * @param record
   * @param type
   */
  @action
  openConfirm = (record, type) => {
    const { id, code } = record;
    this.confirmType = type;
    this.id = id;
    this.istName = code;
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
    const projectId = parseInt(AppState.currentMenuType.id, 10);
    this.confirmLoading = true;
    InstancesStore.changeIstActive(projectId, id, status).then(error => {
      if (error && error.failed) {
        Choerodon.prompt(error.message);
      } else {
        this.loadIstOverview();
        this.closeConfirm();
      }
      this.confirmLoading = false;
    }).catch(e => {
      this.confirmLoading = false;
      Choerodon.handleResponseError(e);
    });
  };

  /**
   * 关闭网络侧边栏
   */
  @action
  closeNetwork = isLoad => {
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
  handleCancel = res => {
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

  closeDeleteModal(id) {
    this.openRemove = false;
  }

  /**
   * 打开删除数据模态框
   */
  @action
  handleOpen(record) {
    const { id, code } = record;
    this.openRemove = true;
    this.id = id;
    this.istName = code;
  }

  /**
   * 删除数据
   */
  @action
  handleDelete = id => {
    const projectId = parseInt(AppState.currentMenuType.id, 10);
    this.loading = true;
    InstancesStore.deleteInstance(projectId, id)
      .then(res => {
        if (res && res.failed) {
          Choerodon.prompt(res.message);
          this.loading = false;
        } else {
          this.openRemove = false;
          this.loading = false;
          this.loadIstOverview();
        }
      })
      .catch(error => {
        this.loading = false;
        Choerodon.handleResponseError(error);
      });
  };

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
    const { store } = this.props;
    const {
      id: currentProjectId,
      organizationId: orgId,
    } = AppState.currentMenuType;
    const ist = store.getIst;
    if (ist) {
      if (ist.devopsEnvPreviewAppDTOS && ist.devopsEnvPreviewAppDTOS.length) {
        return _.map(ist.devopsEnvPreviewAppDTOS, previewApp => {
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
                    <ExpandRow record={detail} />
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
  handlerAction = e => {
    e.stopPropagation();
  };

  /**
   * action 权限控制
   * @param record 行数据
   * @returns {*}
   */
  columnAction = record => {
    const { id: projectId, type, organizationId } = AppState.currentMenuType;
    const {
      intl: { formatMessage },
    } = this.props;
    const { id, status, connect, appVersionId } = record;
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
      type,
      id: projectId,
      organizationId: orgId,
      name,
    } = AppState.currentMenuType;
    const val = store.getVal;
    const prefix = <Icon type="search" onClick={this.onSearch} />;
    const suffix = val ? <Icon type="close" onClick={this.emitEmpty} /> : null;

    const containerDom =
      this.containerArr.length &&
      _.map(this.containerArr, c => (
        <Option key={c.logId} value={`${c.logId}+${c.containerName}`}>
          {c.containerName}
        </Option>
      ));

    const options = {
      readOnly: true,
      lineNumbers: true,
      autofocus: true,
      lineWrapping: true,
      theme: 'base16-dark',
    };

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
            <Modal
              title={`${formatMessage({ id: 'ist.del' })}“${this.istName}”`}
              visible={this.openRemove}
              closable={false}
              footer={[
                <Button
                  key="back"
                  onClick={this.closeDeleteModal.bind(this, this.id)}
                  disabled={this.loading}
                >
                  <FormattedMessage id="cancel" />
                </Button>,
                <Button
                  key="submit"
                  type="danger"
                  loading={this.loading}
                  onClick={this.handleDelete.bind(this, this.id)}
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
          </React.Fragment>
        )}
      </div>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(AppOverview)));
