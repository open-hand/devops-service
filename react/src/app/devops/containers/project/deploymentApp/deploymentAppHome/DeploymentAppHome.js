import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { Select, Button, Radio, Steps, Icon, Tooltip, Input, Form } from 'choerodon-ui';
import { Content, Header, Page, Permission, stores, axios } from '@choerodon/boot';
import _ from 'lodash';
import '../../../main.scss';
import './DeploymentApp.scss';
import YamlEditor from '../../../../components/yamlEditor';
import SelectApp from '../selectApp';
import EnvOverviewStore from '../../../../stores/project/envOverview';
import DepPipelineEmpty from '../../../../components/DepPipelineEmpty/DepPipelineEmpty';
import AppName from '../../../../components/appName';

const RadioGroup = Radio.Group;
const Step = Steps.Step;
const { AppState } = stores;
const Option = Select.Option;
const { Item: FormItem } = Form;
const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 100 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 26 },
  },
};

const uuidv1 = require('uuid/v1');

@observer
class DeploymentAppHome extends Component {
  /**
   * 检查名字的唯一性
   * @param rule
   * @param value
   * @param callback
   */
  checkName = _.debounce((rule, value, callback) => {
    const {
      intl: { formatMessage },
      DeploymentAppStore,
    } = this.props;

    const { id: projectId } = AppState.currentMenuType;

    const pattern = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;

    if (value && !pattern.test(value)) {

      callback(formatMessage({ id: 'network.name.check.failed' }));
      this.setState({ istNameOk: false });

    } else if (value && pattern.test(value)) {

      DeploymentAppStore.checkIstName(projectId, value).then(data => {
        if (data && data.failed) {
          callback(formatMessage({ id: 'network.name.check.exist' }));
          this.setState({ istNameOk: false });
        } else {
          this.setState({ istNameOk: true });
          callback();
        }
      });

    } else {
      this.setState({ istNameOk: true });
      callback();

    }
  }, 1000);

  /**
   * 搜索版本
   * 防抖时间 500ms
   */
  handleVersionSearch = _.debounce(value => {
    const { appId, isLocalProject } = this.state;

    if (appId) {
      const isPublic = !isLocalProject;
      this.setState({ versionSearchParam: value, versionPageNum: 1 });
      this.handleLoadVersion(appId, isPublic, value, value === '');
    }
  }, 500);

  constructor(props) {
    super(props);
    const {
      match: {
        // 路由传参
        params: { appId, verId },
      },
      location: { search },
    } = props;

    this.state = {
      isLocalProject: search.indexOf('notLocalApp') === -1,
      appId: Number(appId) || undefined,
      versionId: Number(verId) || undefined,
      currentStep: appId ? 1 : 0,
      envId: search.split('envId=')[1] ? Number(search.split('envId=')[1]) : undefined,
      showAppSelector: false,
      mode: 'create',
      markers: null,
      loading: false,
      // disabledChangeTopEnv 是限制应用部署时，选定环境后不允许再切换系统环境
      disabledChangeTopEnv: false,
      istNameOk: true,
      istName: '',
      // 下面是和yaml编辑器相关的状态
      hasEditorError: false,
      configValue: null,
      versions: [],
      versionOptions: [],
      versionPageNum: 1,
      versionSearchParam: '',
      versionLoading: false,
      isChangedYaml: false,
    };

    const step = ['One', 'Two', 'Three', 'Four'];
    _.forEach(step, (item, index) => {
      this[`jumpToStep${item}`] = () => this.changeStep(index);
    });
  }


  componentDidMount() {
    const { DeploymentAppStore } = this.props;

    const {
      appId,
      isLocalProject,
    } = this.state;

    this.loadActiveEnv();
    DeploymentAppStore.setValue(null);

    // 如果是从部署总览或环境总览跳转进来
    if (appId) {
      const isPublic = !isLocalProject;

      this.handleLoadVersion(appId, isPublic, '', true);
      this.handleLoadApps(appId);

      // 应用市场和本地应用的版本号可能不同
      // this.setState({versionId: isLocalProject ? versionId : undefined})
    }
  }

  componentWillUnmount() {
    this.props.DeploymentAppStore.setValue(null);
  }

  /**
   * 加载项目下的所已连接的环境
   */
  loadActiveEnv() {
    const { DeploymentAppStore } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const {
      currentStep,
      appId,
      versionId,
    } = this.state;

    EnvOverviewStore.loadActiveEnv(projectId)
      .then(data => {
        if (data && currentStep === 1) {
          const topEnvId = EnvOverviewStore.getTpEnvId;

          // 系统环境是否连接
          const properEnv = _.find(data, { connect: true, id: topEnvId });
          let initEnvId;
          if (!properEnv) {
            // 当前系统中活跃的环境
            // 从部署总览进入应用部署会默认选择环境进行加载配置文件
            // 防止系统环境未连接时，跳转后加载未连接环境的配置文件
            const activeEnvs = _.filter(data, item => item.connect);
            initEnvId = activeEnvs.length ? activeEnvs[0].id : undefined;
          } else {
            initEnvId = properEnv.id;
          }

          this.setState({
            envId: initEnvId,
            envDto: properEnv || data[0],
          });

          DeploymentAppStore.loadInstances(projectId, appId, initEnvId);
          DeploymentAppStore.loadValue(projectId, appId, versionId, initEnvId);
        }
      });
  };

  /**
   * 改变步骤条
   * @param index
   */
  changeStep = index => {
    this.setState({ currentStep: index, disabledChangeTopEnv: false });

    const { id: projectId } = AppState.currentMenuType;
    const { DeploymentAppStore } = this.props;
    const { appId, versionId, envId: id, configValue } = this.state;

    const { getEnvcard, getTpEnvId } = EnvOverviewStore;
    const env = _.filter(getEnvcard, { connect: true, id: getTpEnvId });
    const envId = env.length ? env[0].id : id;

    const page = document.getElementsByClassName('page-content');

    if (index === 1 && appId && versionId && envId) {
      this.setState({ envId, envDto: env[0] });
      DeploymentAppStore.setValue(null);
      DeploymentAppStore.loadValue(projectId, appId, versionId, envId);
      DeploymentAppStore.loadInstances(projectId, appId, envId);
    }

    if (index === 2 || index === 3) {
      this.setState({ disabledChangeTopEnv: true });
      if (!configValue) {
        const configValue = DeploymentAppStore.getValue ? DeploymentAppStore.getValue.yaml : '';
        this.setState({ configValue });
      }
    }

    if (page && page.length) {
      page[0].scrollTop = 0;
    }
  };

  /**
   * 展开选择应用侧边栏
   */
  showSideBar = () => {
    this.setState({ show: true });
  };

  /**
   * 关闭选择应用侧边栏
   */
  handleCancel = () => {
    this.setState({ show: false });
  };

  /**
   * 确认选择APP
   * @param app 选择的数据
   * @param key 标明是项目应用还是应用市场应用
   */
  handleSelectApp = (app, key) => {
    if (app) {
      const isLocalProject = key === '1';
      const appId = isLocalProject ? app.id : app.appId;

      this.setState({
        app,
        appId,
        isLocalProject,
        show: false,
        versionDto: null,
        versionId: undefined,
        istName: `${app.code}-${uuidv1().substring(0, 5)}`,
        versions: [],
        versionOptions: [],
        versionPageNum: 1,
        versionSearchParam: '',
        isChangedYaml: false,
      });

      this.handleLoadVersion(appId, !isLocalProject);
    } else {
      this.setState({ show: false });
    }
  };

  /**
   * 加载应用
   * @param id
   */
  handleLoadApps = (id) => {
    const { DeploymentAppStore } = this.props;
    const { id: projectId } = AppState.currentMenuType;

    DeploymentAppStore.loadApps(projectId, id)
      .then(data => {

        if (data) {
          const istName = data ? `${data.code}-${uuidv1().substring(0, 5)}` : '';

          this.setState({
            app: data,
            istName,
          });
        }

      });
  };

  /**
   * 加载版本
   * @param id
   * @param isPublic
   * @param search
   * @param init
   */
  handleLoadVersion = async (id, isPublic, search = '', init) => {
    const { DeploymentAppStore, intl: { formatMessage } } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const { versionId } = this.state;

    let initValue = '';
    if (init) {
      initValue = versionId;
    }

    try {
      this.setState({ versionLoading: true });
      let isNotEnable = true;
      const data = await DeploymentAppStore.loadVersion(projectId, id, isPublic, 0, search, initValue);
      if (data) {
        const { totalPages, content } = data;

        // 被选中的版本的详细信息
        const versionDto = _.filter(content, v => v.id === versionId)[0];
        const versionOptions = this.renderVersionOptions(content);

        if (totalPages > 1) {
          // 在选项最后置入一个加载更多按钮
          isNotEnable = false;
          versionOptions.push(<Option
            disabled
            className="c7ncd-more-btn-wrap"
            key="btn_load_more"
          >
            <Button
              type="default"
              className="c7ncd-more-btn"
              disabled={isNotEnable}
              onClick={this.handleLoadMoreVersion}
            >
              {formatMessage({ id: 'ist.more' })}
            </Button>
          </Option>);
        }

        this.setState({ versionOptions, versionDto, versions: content, versionLoading: false });
      }
    } catch (e) {
      this.setState({ versionLoading: false });
    }
  };

  /**
   * 点击加载更多
   * @param e
   */
  handleLoadMoreVersion = async (e) => {
    e.stopPropagation();

    const { DeploymentAppStore } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const {
      versionId,
      versionOptions,
      versions,
      appId,
      isLocalProject,
      versionPageNum,
      versionSearchParam,
    } = this.state;

    this.setState({ versionLoading: true });

    try {
      const data = await DeploymentAppStore.loadVersion(projectId, appId, !isLocalProject, versionPageNum, versionSearchParam);
      if (data) {
        const { totalPages, content } = data;

        const moreVersion = _.filter(content, item => item.id !== versionId);

        const options = this.renderVersionOptions(moreVersion);
        const newVersionOpt = _.concat(
          _.initial(versionOptions),
          options,
          versionPageNum + 1 < totalPages ? _.last(versionOptions) : [],
        );
        const newVersions = _.concat(versions, content);

        this.setState({
          versionOptions: newVersionOpt,
          versions: newVersions,
          versionLoading: false,
          versionPageNum: versionPageNum + 1,
        });
      }
    } catch (e) {
      this.setState({ versionLoading: false });
    }
  };

  /**
   * 生成版本选项
   * @param versions
   */
  renderVersionOptions = versions => _.map(versions, version => (
    <Option key={version.id} value={version.id}>
      {version.version}
    </Option>
  ));

  /**
   * 选择环境
   * @param value
   */
  handleSelectEnv = value => {
    const { id: projectId } = AppState.currentMenuType;
    const { DeploymentAppStore } = this.props;
    const { appId, versionId } = this.state;
    const envs = EnvOverviewStore.getEnvcard;
    EnvOverviewStore.setTpEnvId(value);
    DeploymentAppStore.setValue(null);
    const envDto = _.filter(envs, v => v.id === value)[0];
    this.setState({
      envId: value,
      envDto,
      configValue: null,
      mode: 'create',
      markers: [],
      isChangedYaml: false,
    });

    DeploymentAppStore.loadValue(projectId, appId, versionId, value);
    DeploymentAppStore.loadInstances(projectId, appId, value);
  };

  /**
   * 选择版本
   * @param value
   */
  handleSelectVersion = value => {
    const { DeploymentAppStore } = this.props;
    const { envId, versions } = this.state;
    const versionDto = _.filter(versions, v => v.id === value)[0];
    DeploymentAppStore.setValue(null);
    this.setState({
        versionId: value,
        versionDto,
        value: null,
        markers: [],
        isChangedYaml: false,
      }, () => {
        if (envId) {
          this.handleSelectEnv(envId);
        }
      },
    );
  };

  /**
   * 选择实例
   * @param value
   */
  handleSelectInstance = value => {
    const { DeploymentAppStore } = this.props;
    const instance = DeploymentAppStore.currentInstance;
    const instanceDto = _.filter(instance, v => v.id === value)[0];
    this.setState({
      instanceId: value,
      instanceDto,
      istName: instanceDto.code,
    });
    this.props.form.setFields({
      name: {
        value: instanceDto.code,
      },
    });
  };

  /**
   * 修改实例模式
   * @param e
   */
  handleChangeMode = e => {
    const { DeploymentAppStore, form } = this.props;
    const { app, instanceDto } = this.state;
    const instances = DeploymentAppStore.currentInstance;
    if (e.target.value === 'create') {
      const randomString = uuidv1();
      const istName = app.code ? `${app.code.substring(0, 24)}-${randomString.substring(0, 5)}` : randomString.substring(0, 30);
      this.setState({ istName });
      form.setFields({
        name: {
          value: istName,
        },
      });
    } else if (instanceDto) {
      this.setState({ istName: instanceDto.code });
      form.setFields({
        name: {
          value: instanceDto.code,
        },
      });
    } else {
      this.setState({ istName: instances[0].code });
      form.setFields({
        name: {
          value: instances[0].code,
        },
      });
    }
    this.setState({ mode: e.target.value });
    this.handleRenderMode();
  };

  /**
   * 点击取消按钮
   */
  handleStepCancel = () => {
    const {
      DeploymentAppStore,
      history,
      match: {
        params: { prevPage },
      },
    } = this.props;

    DeploymentAppStore.setValue(null);

    const newState = {
      currentStep: 0,
      appId: undefined,
      app: null,
      versionId: undefined,
      versionDto: null,
      envId: undefined,
      envDto: null,
      markers: [],
      mode: 'create',
      instanceId: undefined,
      configValue: null,
      versionOptions: [],
      isChangedYaml: false,
    };

    if (prevPage) {
      history.go(-1);
    } else {
      this.setState(newState);
    }
  };

  /**
   * 部署应用
   * @param flag 标识是否修改了已存在的部署，true 表示没有修改，false表示修改
   */
  handleDeploy = flag => {
    const { DeploymentAppStore } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const { istName, configValue, appId, versionId, envId, mode, instanceId } = this.state;
    const instances = DeploymentAppStore.currentInstance;
    const currentInstanceId = instances && instances.length ? instances[0].id : undefined;
    const appInstanceId = mode === 'create' ? null : (instanceId || currentInstanceId);
    const applicationDeployDTO = {
      isNotChange: flag,
      instanceName: istName,
      appVersionId: versionId,
      environmentId: envId,
      values: configValue,
      type: mode,
      appId,
      appInstanceId,
    };

    this.setState({ loading: true });
    DeploymentAppStore.submitDeployment(projectId, applicationDeployDTO)
      .then(data => {
        this.setState({ loading: false });
        if (data) {
          this.openAppDeployment();
        }
      })
      .catch(error => {
        Choerodon.handleResponseError(error);
        this.setState({ loading: false });
      });
  };

  /**
   * 返回到上一个页面
   */
  openAppDeployment = () => {
    const {
      history,
      match: {
        params: { prevPage },
      },
    } = this.props;

    const { name, id, type, organizationId } = AppState.currentMenuType;

    let url = 'instance';

    switch (prevPage) {
      case 'envOverview':
        url = 'env-overview';
        break;
      case 'deployOverview':
        url = 'deploy-overview';
        break;
      default:
    }

    history.push(
      `/devops/${url}?type=${type}&id=${id}&name=${name}&organizationId=${organizationId}`,
    );
  };


  /**
   * 第四步新建实例名
   */
  handleIstNameChange = e => {
    this.setState({ istNameOk: false, istName: e.target.value });
  };

  /**
   * value 编辑器内容修改
   * @param value
   * @param changed 有效值有无改动
   */
  handleChangeValue = (value, changed = false) => {
    this.setState({ configValue: value, isChangedYaml: changed });
  };

  /**
   * 渲染第一步
   */
  handleRenderApp = () => {
    const { id: projectId, type, organizationId } = AppState.currentMenuType;
    const { intl: { formatMessage } } = this.props;
    const {
      app,
      isLocalProject,
      versionId,
      appId,
      versionOptions,
      versionLoading,
    } = this.state;

    return (
      <Fragment>
        <p className="c7ncd-step-describe">
          {formatMessage({ id: 'deploy.step.one.description' })}
        </p>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-icon" type="widgets" />
            <span className="c7ncd-step-item-title">
              {formatMessage({ id: 'deploy.step.one.app' })}
            </span>
          </div>
          <div className="c7ncd-step-item-indent">
            <div className="c7ncd-step-item-app">
              {app && (
                <AppName
                  width="366px"
                  name={`${app.name}(${app.code})`}
                  showIcon
                  self={isLocalProject}
                />
              )}
            </div>
            <Permission
              organizationId={organizationId}
              projectId={projectId}
              type={type}
              service={[
                'devops-service.application.pageByOptions',
                'devops-service.application-market.listAllApp',
              ]}
            >
              <Button
                className={`c7ncd-detail-btn ${app ? 'c7ncd-detail-btn-right' : ''}`}
                onClick={this.showSideBar}
              >
                <FormattedMessage id="deploy.app.add" />
                <Icon type="open_in_new" />
              </Button>
            </Permission>
          </div>
        </div>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-icon" type="version" />
            <span className="c7ncd-step-item-title">
              {formatMessage({ id: 'deploy.step.one.version.title' })}
            </span>
          </div>
          <div className="c7ncd-step-item-indent">
            <Select
              filter
              className="c7ncd-step-input"
              label={<FormattedMessage id="deploy.step.one.version" />}
              optionFilterProp="children"
              style={{ width: 482 }}
              onSelect={this.handleSelectVersion}
              onSearch={this.handleVersionSearch}
              value={versionId}
              loading={versionLoading}
              notFoundContent={formatMessage({ id: 'network.form.version.notFount' })}
              filterOption={false}
            >
              {versionOptions}
            </Select>
          </div>
        </div>
        <div className="c7ncd-step-btn">
          <Button
            disabled={!(appId && versionId)}
            type="primary"
            funcType="raised"
            onClick={this.jumpToStepTwo}
          >
            <FormattedMessage id="next" />
          </Button>
          <Button
            className="c7ncd-step-cancel-btn"
            funcType="raised"
            onClick={this.handleStepCancel}
          >
            <FormattedMessage id="cancel" />
          </Button>
        </div>
      </Fragment>
    );
  };

  handleSecondNextStepEnable = flag => {
    this.setState({ hasEditorError: flag });
  };

  /**
   * 第二步
   */
  handleRenderEnv = () => {
    const {
      DeploymentAppStore: { getValue },
      intl: { formatMessage },
    } = this.props;
    const {
      configValue,
      envId,
      hasEditorError,
    } = this.state;

    const { getEnvcard, getTpEnvId } = EnvOverviewStore;
    const activeEnv = _.filter(getEnvcard, { connect: true, id: getTpEnvId });

    const envOptions = _.map(getEnvcard, v => (
      <Option value={v.id} key={v.id} disabled={!v.connect || !v.permission}>
        <span
          className={`c7ncd-status c7ncd-status-${v.connect ? 'success' : 'disconnect'}`}
        />
        {v.name}
      </Option>
    ));

    const initValue = getValue ? getValue.yaml : '';
    const enableClick = !(envId && (configValue || initValue) && !hasEditorError);

    return (
      <Fragment>
        <p className="c7ncd-step-describe">
          {formatMessage({ id: 'deploy.step.two.description' })}
        </p>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-icon" type="donut_large" />
            <span className="c7ncd-step-item-title">
              {formatMessage({ id: 'deploy.step.two.env.title' })}
            </span>
          </div>
          <div className="c7ncd-step-item-indent">
            <Select
              className="c7ncd-step-input"
              value={activeEnv.length ? activeEnv[0].id : envId}
              label={formatMessage({ id: 'deploy.step.two.env' })}
              onSelect={this.handleSelectEnv}
              style={{ width: 482 }}
              optionFilterProp="children"
              filterOption={(input, option) =>
                option.props.children[1]
                  .toLowerCase()
                  .indexOf(input.toLowerCase()) >= 0
              }
              filter
            >
              {envOptions}
            </Select>
          </div>
        </div>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-icon" type="description" />
            <span className="c7ncd-step-item-title">
              {formatMessage({ id: 'deploy.step.two.config' })}
            </span>
            <Icon className="c7ncd-step-item-tip-icon" type="error" />
            <span className="c7ncd-step-item-tip-text">
              {formatMessage({ id: 'deploy.step.two.description_1' })}
            </span>
          </div>
          {getValue ? <YamlEditor
            readOnly={false}
            value={configValue || initValue}
            originValue={initValue}
            onValueChange={this.handleChangeValue}
            handleEnableNext={this.handleSecondNextStepEnable}
          /> : null}
        </div>
        <div className="c7ncd-step-btn">
          <Button
            type="primary"
            funcType="raised"
            onClick={this.jumpToStepThree}
            disabled={enableClick}
          >
            <FormattedMessage id="next" />
          </Button>
          <Button onClick={this.jumpToStepOne} funcType="raised">
            <FormattedMessage id="previous" />
          </Button>
          <Button
            funcType="raised"
            className="c7ncd-step-cancel-btn"
            onClick={this.handleStepCancel}
          >
            <FormattedMessage id="cancel" />
          </Button>
        </div>
      </Fragment>
    );
  };

  /**
   * 渲染第三步
   */
  handleRenderMode = () => {
    const {
      DeploymentAppStore: { getCurrentInstance },
      intl: { formatMessage },
      form: { getFieldDecorator },
    } = this.props;
    const { mode, instanceId, istName, istNameOk } = this.state;

    const enableNextStep = !(
      (mode === 'create' && istName && istNameOk) ||
      (mode === 'update' && (instanceId || (getCurrentInstance && getCurrentInstance.length))));

    const getReplaceContent = () => {
      const currentInstanceId = getCurrentInstance && getCurrentInstance.length ? getCurrentInstance[0].id : undefined;
      const existedInstance = _.map(getCurrentInstance, v => (
        <Option value={v.id} key={v.id}>
          {v.code}
        </Option>
      ));
      return (<Select
        filter
        className="c7ncd-step-input"
        optionFilterProp="children"
        onSelect={this.handleSelectInstance}
        value={instanceId || currentInstanceId}
        label={<FormattedMessage id="deploy.step.three.mode.replace.label" />}
        filterOption={(input, option) =>
          option.props.children
            .toLowerCase()
            .indexOf(input.toLowerCase()) >= 0
        }
      >
        {existedInstance}
      </Select>);
    };

    return (
      <Fragment>
        <p className="c7ncd-step-describe">
          {formatMessage({ id: 'deploy.step.three.description' })}
        </p>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-icon" type="jsfiddle" />
            <span className="c7ncd-step-item-title">
              {formatMessage({ id: 'deploy.step.three.mode.title' })}
            </span>
          </div>
          <div className="c7ncd-step-item-indent">
            <RadioGroup
              onChange={this.handleChangeMode}
              value={mode}
              label={<FormattedMessage id="deploy.step.three.mode" />}
            >
              <Radio className="deploy-radio" value="create">
                <FormattedMessage id="deploy.step.three.mode.create" />
              </Radio>
              <Radio
                className="deploy-radio"
                value="update"
                disabled={!getCurrentInstance.length}
              >
                <FormattedMessage id="deploy.step.three.mode.update" />
                <Icon
                  className="c7ncd-step-item-tip-icon"
                  style={{ verticalAlign: 'text-bottom' }}
                  type="error"
                />
                <span
                  className="c7ncd-step-item-tip-text"
                  style={{ verticalAlign: 'unset' }}
                >
                  {formatMessage({ id: 'deploy.step.three.mode.help' })}
                </span>
              </Radio>
            </RadioGroup>
            {mode === 'update' ? getReplaceContent() : null}
          </div>
        </div>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-icon" type="instance_outline" />
            <span className="c7ncd-step-item-title">
              {formatMessage({ id: 'deploy.step.three.ist.title' })}
            </span>
          </div>
          <div className="c7ncd-step-item-indent">
            <Form layout="vertical">
              <FormItem {...formItemLayout}>
                {getFieldDecorator('name', {
                  initialValue: istName,
                  rules: [
                    {
                      required: true,
                      message: formatMessage({ id: 'required' }),
                    },
                    { validator: this.checkName },
                  ],
                })(
                  <Input
                    className="c7ncd-step-input"
                    onChange={this.handleIstNameChange}
                    disabled={mode !== 'create'}
                    maxLength={30}
                    label={formatMessage({ id: 'deploy.instance' })}
                    size="default"
                  />,
                )}
              </FormItem>
            </Form>
          </div>
        </div>
        <div className="c7ncd-step-btn">
          <Button
            type="primary"
            funcType="raised"
            onClick={this.jumpToStepFour}
            disabled={enableNextStep}
          >
            <FormattedMessage id="next" />
          </Button>
          <Button funcType="raised" onClick={this.jumpToStepTwo}>
            <FormattedMessage id="previous" />
          </Button>
          <Button
            funcType="raised"
            className="c7ncd-step-cancel-btn"
            onClick={this.handleStepCancel}
          >
            <FormattedMessage id="cancel" />
          </Button>
        </div>
      </Fragment>
    );
  };

  /**
   * 渲染第四步预览
   * @returns {*}
   */
  handleRenderReview = () => {
    const {
      DeploymentAppStore: { getCurrentInstance },
    } = this.props;
    const {
      app,
      versionId,
      envId,
      versionDto,
      envDto,
      mode,
      instanceDto,
      instanceId,
      istName,
      loading,
      configValue,
      isChangedYaml,
    } = this.state;

    const currentInstanceId = getCurrentInstance && getCurrentInstance.length ? getCurrentInstance[0].id : undefined;
    const currentInstanceCode = getCurrentInstance && getCurrentInstance.length ? getCurrentInstance[0].code : undefined;
    const instance = instanceDto || _.filter(getCurrentInstance, v => v.id === instanceId || currentInstanceId)[0];

    // 改变配置信息/替换模式且版本不同
    const isNotChanged = !isChangedYaml && (mode === 'update' && versionDto.version === instance.appVersion);
    const deployInfo = [
      {
        icon: 'instance_outline',
        label: 'deploy.instance',
        value: istName,
      }, {
        icon: 'widgets',
        label: 'deploy.step.four.app',
        value: (
          <Fragment>
            {app ? app.name : null}
            <span className="c7ncd-step-info-item-text">
              ({app ? app.code : null})
            </span>
          </Fragment>
        ),
      }, {
        icon: 'version',
        label: 'deploy.step.four.version',
        value: versionDto ? versionDto.version : null,
      }, {
        icon: 'donut_large',
        label: 'deploy.step.two.env.title',
        value: (
          <Fragment>
            {envDto ? envDto.name : null}
            <span className="c7ncd-step-info-item-text">
              ({envDto ? envDto.code : null})
            </span>
          </Fragment>
        ),
      }, {
        icon: 'jsfiddle',
        label: 'deploy.step.three.mode',
        value: (
          <Fragment>
            <FormattedMessage id={`deploy.step.three.mode.${mode}`} />
            {mode === 'update' ? (
              <span className="c7ncd-step-info-item-text">
                ({instanceId ? instanceDto.code : currentInstanceCode})
              </span>
            ) : null}
          </Fragment>
        ),
      }, {
        icon: 'description',
        label: 'deploy.step.two.config',
        value: null,
      },
    ];

    const infoDom = _.map(deployInfo, item => {
      const { icon, label, value } = item;
      return (
        <div key={label} className="c7ncd-step-info-item">
          <div className="c7ncd-step-info-item-label">
            <Icon type={icon} className="c7ncd-step-info-item-icon" />
            <FormattedMessage id={label} />：
          </div>
          {value ? (
            <div className="c7ncd-step-info-item-value">{value}</div>
          ) : null}
        </div>
      );
    });

    return (
      <Fragment>
        <div className="c7ncd-step-item c7ncd-step-item-full">
          {infoDom}
          <YamlEditor readOnly value={configValue} />
        </div>
        <div className="c7ncd-step-btn">
          <Permission service={['devops-service.application-instance.deploy']}>
            <Button
              type="primary"
              funcType="raised"
              disabled={!(app && versionId && envId && mode)}
              onClick={() => this.handleDeploy(isNotChanged)}
              loading={loading}
            >
              <FormattedMessage id="deploy.btn.deploy" />
            </Button>
          </Permission>
          <Button funcType="raised" onClick={this.jumpToStepThree}>
            <FormattedMessage id="previous" />
          </Button>
          <Button
            funcType="raised"
            className="c7ncd-step-cancel-btn"
            onClick={this.handleStepCancel}
          >
            <FormattedMessage id="cancel" />
          </Button>
        </div>
      </Fragment>
    );
  };

  /**
   * 环境选择请求函数
   * @param value
   */
  handleEnvSelect = value => {
    const { currentStep } = this.state;
    const envs = EnvOverviewStore.getEnvcard;
    const env = _.filter(envs, { connect: true, id: value });
    EnvOverviewStore.setTpEnvId(value);
    if (currentStep === 1 && env && env.length) {
      this.handleSelectEnv(value);
    }
  };

  render() {
    const { intl: { formatMessage } } = this.props;
    const { name: projectName } = AppState.currentMenuType;
    const {
      currentStep,
      disabledChangeTopEnv,
      show,
      isLocalProject,
      app,
      isChangedYaml,
    } = this.state;

    const { getTpEnvId, getEnvcard: envData } = EnvOverviewStore;
    const STEP_LIST = ['one', 'two', 'three', 'four'];
    const stepDom = _.map(STEP_LIST, item => (
      <Step key={item} title={formatMessage({ id: `deploy.step.${item}.title` })} />
    ));

    const stepRender = [
      this.handleRenderApp,
      this.handleRenderEnv,
      this.handleRenderMode,
      this.handleRenderReview,
    ];

    const hasEnvAndPermission = envData && envData.length && getTpEnvId;
    const permission = [
      'devops-service.application.queryByAppId',
      'devops-service.application-version.queryByAppId',
      'devops-service.devops-environment.listByProjectIdAndActive',
      'devops-service.application-instance.queryValues',
      'devops-service.application-instance.formatValue',
      'devops-service.application-instance.listByAppIdAndEnvId',
      'devops-service.application-instance.deploy',
      'devops-service.application.pageByOptions',
      'devops-service.application-market.listAllApp',
      'devops-service.application-instance.previewValues',
    ];

    return (
      <Page service={permission}>
        {hasEnvAndPermission ? (
          <Fragment>
            <Header title={<FormattedMessage id="deploy.header.title" />}>
              <Select
                className={`${
                  getTpEnvId
                    ? 'c7n-header-select'
                    : 'c7n-header-select c7n-select_min100'
                  }`}
                dropdownClassName="c7n-header-env_drop"
                placeholder={formatMessage({ id: 'envoverview.noEnv' })}
                value={envData && envData.length ? getTpEnvId : undefined}
                disabled={disabledChangeTopEnv || (envData && envData.length === 0)}
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
            </Header>
            <Content
              className="c7n-deploy-wrapper c7ncd-step-page"
              code="deploy"
              values={{ name: projectName }}
            >
              <div className="c7ncd-step-wrap">
                <Steps className="c7ncd-step-bar" current={currentStep}>
                  {stepDom}
                </Steps>
                <div className="c7ncd-step-card">{stepRender[currentStep]()}</div>
              </div>
              {show && (
                <SelectApp
                  isMarket={!isLocalProject}
                  app={app}
                  show={show}
                  handleCancel={this.handleCancel}
                  handleOk={this.handleSelectApp}
                />
              )}
            </Content>
          </Fragment>
        ) : (
          <DepPipelineEmpty
            title={<FormattedMessage id="deploy.header.title" />}
            type="env"
          />
        )}
      </Page>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(DeploymentAppHome)));
