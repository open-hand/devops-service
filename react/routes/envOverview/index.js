/* eslint-disable react/sort-comp */
import React, { Component, Fragment } from 'react/index';
import { observer } from 'mobx-react';
import { observable, action, configure } from 'mobx';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Button,
  Tabs,
  Form,
  Select,
  Icon,
  Tooltip,
  Menu,
  Dropdown,
} from 'choerodon-ui';
import {
  Content,
  Header,
  Page,
  Permission,
  stores,
} from '@choerodon/boot';
import _ from 'lodash';
import AppOverview from './app-overview';
import LogOverview from './log-overview';
import DomainOverview from './domain-overview';
import NetworkOverview from './network-overview';
import CertTable from '../certificatePro/CertTable';
import CreateDomain from '../domain/domain-create';
import CreateNetwork from '../network/network-create';
import CertificateCreate from '../certificatePro/CertificateCreate';
import DomainStore from '../domain/stores';
import NetworkConfigStore from '../network/stores';
import CertificateStore from '../certificatePro/stores';
import DepPipelineEmpty from '../../components/DepPipelineEmpty/DepPipelineEmpty';
import RefreshBtn from '../../components/refreshBtn';
import DevopsStore from '../../stores/DevopsStore';
import EnvOverviewStore from './stores';

import './index.scss';
import '../main.scss';

const { AppState } = stores;
const { TabPane } = Tabs;
const { Option } = Select;

configure({ enforceActions: 'never' });

@observer
class EnvOverviewHome extends Component {
  @observable env = [];

  @observable showDomain = false;

  @observable showNetwork = false;

  @observable domainId = null;

  @observable domainType = '';

  @observable domainTitle = '';

  constructor(props, context) {
    super(props, context);
    this.state = {
      createDisplay: false,
    };
  }

  componentWillMount() {
    this.loadEnvCards();
  }

  componentWillUnmount() {
    EnvOverviewStore.setIst(null);
    EnvOverviewStore.setTabKey('app');
    DevopsStore.clearAutoRefresh();
  }

  /**
   * 刷新函数重调用 tabChange
   */
  handleRefresh = (spin = true) => {
    EnvOverviewStore.setVal('');
    const {
      getTabKey,
      getTpEnvId,
      getInfo: { filters, sort, paras },
    } = EnvOverviewStore;
    const sorter = { field: '', order: 'desc' };
    if (sort.column) {
      sorter.field = sort.field || sort.columnKey;
      if (sort.order === 'ascend') {
        sorter.order = 'asc';
      }
    }
    let searchParam = {};
    if (Object.keys(filters).length) {
      searchParam = filters;
    }
    const postData = {
      searchParam,
      param: paras.toString(),
    };
    if (this.env.length) {
      this.loadModuleDate(spin, getTabKey, getTpEnvId, sorter, postData);
    }
  };

  /**
   * tab 切换函数
   * @param key
   */
  @action
  tabChange = key => {
    EnvOverviewStore.setTabKey(key);
    const envId = EnvOverviewStore.getTpEnvId;
    const sort = { field: 'id', order: 'desc' };
    const post = {
      searchParam: {},
      param: '',
    };
    const { pageSize } = EnvOverviewStore.getPageInfo;
    EnvOverviewStore.setPageInfo({
      pageNum: 1,
      pageSize: pageSize,
      total: 0,
    });
    if (this.env.length && envId) {
      this.loadModuleDate(true, key, envId, sort, post);
    }
    EnvOverviewStore.setInfo({
      filters: {},
      sort: { columnKey: 'id', order: 'descend' },
      paras: [],
    });
  };

  loadModuleDate = (spin, key, env, sort, post) => {
    this.loadSync(env);
    this.loadLog(spin, env);
    switch (key) {
      case 'domain':
        this.loadDomainOrNet(spin, 'domain', env, sort, post);
        break;
      case 'network':
        this.loadDomainOrNet(spin, 'net', env, sort, post);
        break;
      case 'log':
        break;
      case 'cert':
        this.loadCertData(spin, env);
        break;
      default:
        this.loadIstOverview(spin, env);
        break;
    }
  };

  /**
   * 环境选择请求函数
   * @param value
   */
  @action
  handleEnvSelect = value => {
    EnvOverviewStore.setTpEnvId(value);
    this.loadAllDate(value);
  };

  /**
   * 获取可用环境
   */
  @action
  loadEnvCards = () => {
    const projectId = AppState.currentMenuType.id;
    EnvOverviewStore.loadActiveEnv(projectId).then(env => {
      if (env.length) {
        const envId = EnvOverviewStore.getTpEnvId;
        this.env = env;
        if (envId) {
          this.loadAllDate(envId);
        }
      }
    });
  };

  /**
   * 加载应用实例列表
   * @param envId
   */
  loadIstOverview = (spin, envId) => {
    const projectId = AppState.currentMenuType.id;
    EnvOverviewStore.loadIstOverview(spin, projectId, envId);
  };

  /**
   * 按环境加载域名
   */
  loadDomainOrNet = (
    spin,
    type,
    envId,
    sort = { field: 'id', order: 'desc' },
    datas = {
      searchParam: {},
      param: '',
    },
  ) => {
    const { id: projectId } = AppState.currentMenuType;
    const pagination = EnvOverviewStore.getPageInfo;
    if (type === 'domain') {
      EnvOverviewStore.loadDomain(
        spin,
        projectId,
        envId,
        pagination.current,
        pagination.pageSize,
        sort,
        datas,
      );
    } else if (type === 'net') {
      EnvOverviewStore.loadNetwork(
        spin,
        projectId,
        envId,
        pagination.current,
        pagination.pageSize,
        sort,
        datas,
      );
    }
  };

  /**
   * 按环境加载错误日志
   * @param envId
   */
  loadLog = (spin, envId) => {
    const projectId = AppState.currentMenuType.id;
    EnvOverviewStore.loadLog(spin, projectId, envId);
  };

  /**
   * 按环境加载同步状态
   * @param envId
   */
  loadSync = envId => {
    const projectId = AppState.currentMenuType.id;
    EnvOverviewStore.loadSync(projectId, envId);
  };

  /**
   * 加载证书
   * @param spin
   * @param envId
   */
  loadCertData = (spin, envId) => {
    const { id: projectId } = AppState.currentMenuType;
    CertificateStore.loadCertData(spin, projectId, envId);
  };

  loadAllDate(envId) {
    this.loadIstOverview(true, envId);
    this.loadDomainOrNet(true, 'domain', envId);
    this.loadDomainOrNet(true, 'net', envId);
    this.loadLog(true, envId);
    this.loadSync(envId);
    this.loadCertData(true, envId);
  }

  /**
   *打开域名创建弹框
   */
  @action
  createDomain = (type, id = '') => {
    const {
      form,
      intl: { formatMessage },
    } = this.props;
    form.resetFields();
    if (type === 'create') {
      this.domainTitle = formatMessage({
        id: 'domain.header.create',
      });
      this.domainType = type;
      this.domainId = id;
    } else {
      this.domainTitle = formatMessage({
        id: 'domain.header.update',
      });
      this.domainType = type;
      this.domainId = id;
    }
    this.showDomain = true;
  };

  /**
   * 打开创建域名
   */
  @action
  createNetwork = () => {
    this.showNetwork = true;
  };

  /**
   * 关闭域名侧边栏
   */
  @action
  closeDomain = isLoad => {
    this.props.form.resetFields();
    this.showDomain = false;
    this.domainId = null;
    if (isLoad) {
      const envId = EnvOverviewStore.getTpEnvId;
      this.loadDomainOrNet(true, 'domain', envId);
      EnvOverviewStore.setInfo({
        filters: {},
        sort: { columnKey: 'id', order: 'descend' },
        paras: [],
      });
      EnvOverviewStore.setTabKey('domain');
    }
  };

  /**
   * 关闭网络侧边栏
   */
  @action
  closeNetwork = isLoad => {
    this.props.form.resetFields();
    this.showNetwork = false;
    if (isLoad) {
      const envId = EnvOverviewStore.getTpEnvId;
      this.loadDomainOrNet(true, 'net', envId);
      EnvOverviewStore.setInfo({
        filters: {},
        sort: { columnKey: 'id', order: 'descend' },
        paras: [],
      });
      EnvOverviewStore.setTabKey('network');
    }
  };

  /**
   * 创建证书侧边栏
   */
  openCreateModal = () => {
    CertificateStore.setEnvData([]);
    this.setState({ createDisplay: true });
  };

  /**
   * 关闭证书侧边栏
   */
  closeCreateModal = isLoad => {
    this.setState({ createDisplay: false });
    this.props.form.resetFields();
    if (isLoad) {
      const envId = EnvOverviewStore.getTpEnvId;
      this.loadCertData(true, envId);
      EnvOverviewStore.setInfo({
        filters: {},
        sort: { columnKey: 'id', order: 'descend' },
        paras: [],
      });
      EnvOverviewStore.setTabKey('cert');
    }
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
   * 条件部署应用
   */
  deployApp = () => {
    const {
      history,
      location: {
        search,
      },
    } = this.props;

    history.push({
      pathname: '/devops/deployment-app',
      search,
      state: {
        prevPage: 'env',
      },
    });
  };

  /**
   * 遍历计算实例运行状态总览数据
   * @param state
   * @returns {number}
   */
  getIstCount = state => {
    const ist = EnvOverviewStore.getIst;
    const stateArr = ist
      ? _.map(ist.devopsEnvPreviewAppDTOS, i =>
        _.filter(i.applicationInstanceDTOS, a => a.status === state),
      )
      : [];
    let length = 0;
    _.map(stateArr, l => {
      length += l.length;
    });
    return length;
  };

  /**
   * 点击失败状态跳转到日志tab页
   */
  linkToLogTabs = () => {
    EnvOverviewStore.setTabKey('log');
  };

  render() {
    const {
      intl: { formatMessage },
    } = this.props;
    const { createDisplay } = this.state;
    const envId = EnvOverviewStore.getTpEnvId;
    const envData = EnvOverviewStore.getEnvcard;
    const tabKey = EnvOverviewStore.getTabKey;
    const log = EnvOverviewStore.getLog;
    const sync = EnvOverviewStore.getSync;
    const {
      type,
      id: projectId,
      organizationId: orgId,
      name,
    } = AppState.currentMenuType;

    const envState = this.env.length
      ? this.env.filter(d => d.id === Number(envId))[0]
      : { connect: false };

    if (envData && envData.length && envId) {
      DevopsStore.initAutoRefresh('overview', this.handleRefresh);
    }

    // tab页选项
    const tabOption = [
      {
        key: 'app',
        component: (
          <AppOverview
            store={EnvOverviewStore}
            tabkey={tabKey}
            envState={envState && envState.connect}
            envId={envId}
          />
        ),
        msg: 'network.column.app',
      },
      {
        key: 'network',
        component: (
          <NetworkOverview
            store={EnvOverviewStore}
            tabkey={tabKey}
            envId={envId}
          />
        ),
        msg: 'network.header.title',
      },
      {
        key: 'domain',
        component: (
          <DomainOverview
            store={EnvOverviewStore}
            tabkey={tabKey}
            envId={envId}
          />
        ),
        msg: 'domain.header.title',
      },
      {
        key: 'cert',
        component: <CertTable store={CertificateStore} envId={envId} />,
        msg: 'ctf.head',
      },
      {
        key: 'log',
        component: (
          <LogOverview store={EnvOverviewStore} tabkey={tabKey} envId={envId} />
        ),
        msg: 'envoverview.logs',
      },
    ];

    const istStatusType = ['running', 'operating', 'stopped', 'failed'];

    const menu = (
      <Menu className="c7n-envow-dropdown-link">
        <Menu.Item key="0" disabled={envState && !envState.connect}>
          <Permission
            service={['devops-service.devops-service.create']}
            type={type}
            projectId={projectId}
            organizationId={orgId}
          >
            <Tooltip
              title={
                envState && !envState.connect ? (
                  <FormattedMessage id="envoverview.envinfo" />
                ) : null
              }
            >
              <Button
                funcType="flat"
                disabled={envState && !envState.connect}
                onClick={this.createNetwork}
              >
                <FormattedMessage id="network.header.create" />
              </Button>
            </Tooltip>
          </Permission>
        </Menu.Item>
        <Menu.Item key="1" disabled={envState && !envState.connect}>
          <Permission
            service={['devops-service.devops-ingress.create']}
            type={type}
            projectId={projectId}
            organizationId={orgId}
          >
            <Tooltip
              title={
                envState && !envState.connect ? (
                  <FormattedMessage id="envoverview.envinfo" />
                ) : null
              }
            >
              <Button
                funcType="flat"
                disabled={envState && !envState.connect}
                onClick={this.createDomain.bind(this, 'create', '')}
              >
                <FormattedMessage id="domain.header.create" />
              </Button>
            </Tooltip>
          </Permission>
        </Menu.Item>
        <Menu.Item key="3" disabled={envState && !envState.connect}>
          <Permission
            type={type}
            projectId={projectId}
            organizationId={orgId}
            service={['devops-service.certification.create']}
          >
            <Tooltip
              title={
                envState && !envState.connect ? (
                  <FormattedMessage id="envoverview.envinfo" />
                ) : null
              }
            >
              <Button
                funcType="flat"
                disabled={envState && !envState.connect}
                onClick={this.openCreateModal}
              >
                <FormattedMessage id="ctf.create" />
              </Button>
            </Tooltip>
          </Permission>
        </Menu.Item>
      </Menu>
    );

    let syncDom = null;

    if (log && log.length) {
      syncDom = (
        <div className="c7n-envow-sync-wrap">
          <div className="c7n-envow-status-text">
            <FormattedMessage id="envoverview.error" />
          </div>
          <div>
            <Button
              ghost
              funcType="flat"
              shape="circle"
              onClick={this.linkToLogTabs}
            >
              <Icon type="cancel" className="c7n-envow-error-icon" />
            </Button>
          </div>
        </div>
      );
    } else if (
      sync &&
      (sync.devopsSyncCommit !== sync.sagaSyncCommit ||
        sync.sagaSyncCommit !== sync.agentSyncCommit) &&
      envState &&
      envState.connect
    ) {
      syncDom = (
        <div className="c7n-envow-sync-wrap">
          <div className="c7n-envow-status-text">
            <FormattedMessage id="envoverview.sync" />
          </div>
          <div className="c7n-envow-sync-icon">
            <Icon type="autorenew" />
          </div>
        </div>
      );
    }

    return (
      <Page
        className="c7n-region c7n-app-wrapper"
        service={[
          'devops-service.application-instance.listByAppId',
          'devops-service.application-instance.listByAppInstanceId',
          'devops-service.application-instance.queryValue',
          'devops-service.application-instance.deploy',
          'devops-service.application-instance.pageInstances',
          'devops-service.application-instance.pageByOptions',
          'devops-service.application-instance.listByAppVersionId',
          'devops-service.application-instance.queryValues',
          'devops-service.application-instance.listResources',
          'devops-service.application-instance.listStages',
          'devops-service.application-instance.delete',
          'devops-service.application-instance.start',
          'devops-service.application-instance.stop',
          'devops-service.application-instance.restart',
          'devops-service.application-instance.listByEnv',
          'devops-service.application-version.getUpgradeAppVersion',
          'devops-service.devops-env-file-error.page',
          'devops-service.devops-environment.listByProjectIdAndActive',
          'devops-service.application.listByEnvIdAndStatus',
          'devops-service.devops-service.create',
          'devops-service.devops-service.checkName',
          'devops-service.devops-service.pageByOptions',
          'devops-service.devops-service.query',
          'devops-service.devops-service.update',
          'devops-service.devops-service.delete',
          'devops-service.devops-service.listByEnv',
          'devops-service.devops-ingress.pageByOptions',
          'devops-service.devops-ingress.queryDomainId',
          'devops-service.devops-ingress.delete',
          'devops-service.devops-ingress.create',
          'devops-service.devops-ingress.listByEnv',
          'devops-service.certification.listByOptions',
          'devops-service.certification.create',
          'devops-service.certification.delete',
          'devops-service.devops-environment.queryByCode',
        ]}
      >
        {envData && envData.length && envId ? (
          <Fragment>
            <Header title={<FormattedMessage id="envoverview.head" />}>
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
              <Permission
                service={['devops-service.devops-service.create']}
                type={type}
                projectId={projectId}
                organizationId={orgId}
              >
                <div className="c7n-envow-select">
                  <Dropdown overlay={menu} trigger={['click']}>
                    <Button
                      className="c7ncd-detail-btn-big"
                      icon="playlist_add"
                    >
                      <FormattedMessage id="create" />
                      <Icon type="arrow_drop_down" />
                    </Button>
                  </Dropdown>
                </div>
              </Permission>
              <Permission
                service={['devops-service.application-instance.deploy']}
                type={type}
                projectId={projectId}
                organizationId={orgId}
              >
                <Tooltip
                  title={
                    envState && !envState.connect ? (
                      <FormattedMessage id="envoverview.envinfo" />
                    ) : null
                  }
                >
                  <Button
                    disabled={(envState && !envState.connect) || !envId}
                    onClick={this.deployApp.bind(this, envId)}
                    icon="jsfiddle"
                  >
                    <FormattedMessage id="deploy.header.title" />
                  </Button>
                </Tooltip>
              </Permission>
              <Tooltip
                title={
                  sync && sync.commitUrl
                    ? sync.commitUrl.substr(0, sync.commitUrl.length - 8)
                    : null
                }
              >
                <a
                  className="c7n-envow-gitlab"
                  href={
                    sync && sync.commitUrl
                      ? sync.commitUrl.substr(0, sync.commitUrl.length - 8)
                      : null
                  }
                  target="_blank"
                  rel="nofollow me noopener noreferrer"
                >
                  <Button funcType="flat" icon="account_balance">
                    <FormattedMessage id="envoverview.gitlab" />
                  </Button>
                </a>
              </Tooltip>
              <RefreshBtn name="overview" onFresh={this.handleRefresh} />
            </Header>
            <Content>
              <div className="c7n-envow-status-wrap">
                <div>
                  <h2 className="c7n-space-first">
                    {envId ? (
                      <FormattedMessage
                        id="envoverview.title"
                        values={{
                          name: `${envState && envState.name}`,
                        }}
                      />
                    ) : (
                      <FormattedMessage
                        id="envoverview.noenv.title"
                        values={{
                          name: `${name}`,
                        }}
                      />
                    )}
                  </h2>
                  <p>
                    <FormattedMessage id="envoverview.description" />
                    <a
                      href={formatMessage({ id: 'envoverview.link' })}
                      rel="nofollow me noopener noreferrer"
                      target="_blank"
                      className="c7n-external-link"
                    >
                      <span className="c7n-external-link-content">
                        <FormattedMessage id="learnmore" />
                      </span>
                      <i className="icon icon-open_in_new c7ncd-link-icon" />
                    </a>
                  </p>
                </div>
                <div className="c7n-envow-status-content">
                  {syncDom}
                  <div>
                    <div className="c7n-envow-status-text">
                      <FormattedMessage id="envoverview.istov" />
                    </div>
                    <div className="c7n-envow-status-wrap">
                      {_.map(istStatusType, item => (
                        <div
                          key={item}
                          className={`c7n-envow-status-num c7n-envow-status-${item}`}
                        >
                          <div>{this.getIstCount(item)}</div>
                          <div>
                            <FormattedMessage id={item} />
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
              <Tabs
                className="c7n-envoverview-tabs"
                activeKey={tabKey}
                animated={false}
                onChange={this.tabChange}
              >
                {_.map(tabOption, item => {
                  const { key, component, msg } = item;
                  return (
                    <TabPane tab={formatMessage({ id: msg })} key={key}>
                      {tabKey === key ? component : null}
                    </TabPane>
                  );
                })}
              </Tabs>
            </Content>
          </Fragment>
        ) : (
          <DepPipelineEmpty
            title={<FormattedMessage id="envoverview.head" />}
            type="env"
          />
        )}
        {this.showNetwork && (
          <CreateNetwork
            visible={this.showNetwork}
            store={NetworkConfigStore}
            envId={envId}
            onClose={this.closeNetwork}
          />
        )}
        {this.showDomain && (
          <CreateDomain
            id={this.domainId}
            envId={envId}
            title={this.domainTitle}
            visible={this.showDomain}
            type={this.domainType}
            store={DomainStore}
            onClose={this.closeDomain}
          />
        )}
        {createDisplay ? (
          <CertificateCreate
            visible={createDisplay}
            envId={envId}
            store={CertificateStore}
            onClose={this.closeCreateModal}
          />
        ) : null}
      </Page>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(EnvOverviewHome)));
