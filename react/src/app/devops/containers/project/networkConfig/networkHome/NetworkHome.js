import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Table,
  Button,
  Form,
  Tooltip,
  Popover,
  Icon,
  Select,
} from 'choerodon-ui';
import {
  Permission,
  Content,
  Header,
  Page,
} from '@choerodon/boot';
import _ from 'lodash';
import LoadingBar from '../../../../components/loadingBar';
import CreateNetwork from '../createNetwork';
import EditNetwork from '../editNetwork';
import { commonComponent } from '../../../../components/commonFunction';
import StatusIcon from '../../../../components/StatusIcon';
import EnvOverviewStore from '../../../../stores/project/envOverview';
import DepPipelineEmpty from '../../../../components/DepPipelineEmpty/DepPipelineEmpty';
import RefreshBtn from '../../../../components/refreshBtn';
import EnvFlag from '../../../../components/envFlag';
import DeleteModal from '../../../../components/deleteModal';

import './NetworkHome.scss';
import '../../../main.scss';

const { Option } = Select;

@Form.create({})
@withRouter
@injectIntl
@inject('AppState')
@commonComponent('NetworkConfigStore')
@observer
export default class NetworkHome extends Component {
  state = {
    show: false,
    submitting: false,
    deleteArr: [],
  };

  componentDidMount() {
    const {
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;

    EnvOverviewStore.loadActiveEnv(projectId).then(env => {
      if (env.length) {
        const envId = EnvOverviewStore.getTpEnvId;
        if (envId) {
          this.loadAllData(1, envId);
        }
      }
    });
  }

  componentWillUnmount() {
    const { NetworkConfigStore } = this.props;
    this.clearAutoRefresh();
    this.clearFilterInfo();
    NetworkConfigStore.setAllData([]);
  }

  /**
   * 关闭侧边栏
   */
  handleCancelFun = isload => {
    const { NetworkConfigStore } = this.props;
    this.setState({ show: false, showEdit: false });
    if (isload) {
      NetworkConfigStore.setInfo({
        filters: {},
        sort: { columnKey: 'id', order: 'descend' },
        paras: [],
      });
      const envId = EnvOverviewStore.getTpEnvId;
      this.loadAllData(1, envId);
    }
  };

  /**
   * 打开创建操作框
   */
  showSideBar = () => {
    this.clearStoreData();
    this.setState({ show: true });
  };

  /**
   * 打开编辑的操作框
   * @param id
   */
  editNetwork = id => {
    this.clearStoreData();
    this.setState({ showEdit: true, id });
  };

  /**
   * 清除缓存数据
   */
  clearStoreData = () => {
    const { NetworkConfigStore } = this.props;
    NetworkConfigStore.setApp([]);
    NetworkConfigStore.setEnv([]);
    NetworkConfigStore.setIst([]);
  };

  /**
   * 配置类型 列
   * @param record
   * @returns {Array}
   */
  configColumn = (record) => {
    const { config, type, loadBalanceIp } = record;
    const { externalIps, ports } = config;
    const iPArr = [];
    const portArr = [];
    if (externalIps && externalIps.length) {
      _.forEach(externalIps, item =>
        iPArr.push(
          <div key={item} className="network-config-item">
            {item}
          </div>,
        ),
      );
    }
    if (ports && ports.length) {
      _.forEach(ports, item => {
        const { nodePort, port, targetPort } = item;
        portArr.push(
          <div key={port} className="network-config-item">
            {nodePort || (type !== 'ClusterIP' && <FormattedMessage id="null" />)} {port} {targetPort}
          </div>,
        );
      });
    }
    let content = null;
    switch (type) {
      case 'ClusterIP':
        content = (
          <Fragment>
            <div className="network-config-wrap">
              <div className="network-type-title">
                <FormattedMessage id="network.column.ip" />
              </div>
              <div>{externalIps ? iPArr : '-'}</div>
            </div>
            <div className="network-config-wrap">
              <div className="network-type-title">
                <FormattedMessage id="network.column.port" />
              </div>
              <div>{portArr}</div>
            </div>
          </Fragment>
        );
        break;
      case 'NodePort':
        content = (
          <Fragment>
            <div className="network-config-item">
              <FormattedMessage id="network.node.port" />
            </div>
            <div>{portArr}</div>
          </Fragment>
        );
        break;
      case 'LoadBalancer':
        content = (
          <Fragment>
            <div className="network-config-wrap">
              <div className="network-type-title">
                <FormattedMessage id="network.node.port" />
              </div>
              <div>{portArr}</div>
            </div>
            {loadBalanceIp && (
              <div className="network-config-wrap">
                <div className="network-type-title">
                  <span>LoadBalancer IP</span>
                </div>
                <div>{loadBalanceIp}</div>
              </div>
            )}
          </Fragment>
        );
        break;
      default:
        break;
    }

    return (
      <div className="network-column-config">
        <span className="network-config-type">{type}</span>
        <Popover
          arrowPointAtCenter
          placement="bottomRight"
          getPopupContainer={triggerNode => triggerNode.parentNode}
          content={content}
        >
          <Icon type="expand_more" className="network-expend-icon" />
        </Popover>
      </div>
    );
  };

  /**
   * 生成 目标对象类型 列
   * @param record
   */
  targetTypeColumn = ({ appId, target }) => {
    const { intl: { formatMessage } } = this.props;

    const { appInstance, labels } = target || {};

    let targetType = 'endPoints';
    if (appId && appInstance && appInstance.length) {
      targetType = 'instance';
    } else if (labels) {
      targetType = 'param';
    }

    const message = {
      instance: formatMessage({ id: 'ist.head' }),
      param: formatMessage({ id: 'branch.issue.label' }),
      endPoints: 'EndPoints',
    };

    return <span>{message[targetType]}</span>;
  };

  /**
   * 生成 目标对象 列
   * @param record
   * @returns {Array}
   */
  targetColumn = (record) => {
    const { appInstance, labels, endPoints } = record.target;
    const node = [];
    const port = [];
    const len = endPoints ? 2 : 1;
    if (appInstance && appInstance.length) {
      _.forEach(appInstance, item => {
        const { id, code, instanceStatus } = item;
        const statusStyle =
          instanceStatus !== 'operating' && instanceStatus !== 'running'
            ? 'c7n-network-status-failed'
            : '';
        if (code) {
          node.push(
            <div className={`network-column-instance ${statusStyle}`} key={id}>
              <Tooltip
                title={
                  instanceStatus ? (
                    <FormattedMessage id={instanceStatus} />
                  ) : (
                    <FormattedMessage id="network.ist.deleted" />
                  )
                }
                placement="top"
              >
                {code}
              </Tooltip>
            </div>,
          );
        }
      });
    }
    if (!_.isEmpty(labels)) {
      _.forEach(labels, (value, key) =>
        node.push(
          <div className="network-column-instance" key={key}>
            <span>{key}</span>=<span>{value}</span>
          </div>,
        ),
      );
    }
    if (endPoints) {
      const targetIps = _.split(_.keys(endPoints)[0], ',');
      const portList = _.values(endPoints)[0];
      _.map(targetIps, (item, index) =>
        node.push(
          <div className="network-column-instance" key={index}>
            <span>{item}</span>
          </div>,
        ),
      );
      _.map(portList, (item, index) => {
        port.push(
          <div className="network-column-instance" key={index}>
            <span>{item.port}</span>
          </div>,
        );
      });
    }
    return (
      <Fragment>
        {
          _.map([node, port], (item, index) => (
            <div className="network-column-target" key={index}>
              {item[0] || null}
              {endPoints && (<div className="network-column-targetIp">{item[1] || null}</div>)}
              {item.length > len && (
                <Popover
                  arrowPointAtCenter
                  placement="bottomRight"
                  getPopupContainer={triggerNode => triggerNode.parentNode}
                  content={<Fragment>{item}</Fragment>}
                >
                  <Icon type="expand_more" className="network-expend-icon" />
                </Popover>
              )}
            </div>),
          )
        }
      </Fragment>
    );
  };

  /**
   *
   * @param status
   * @param envStatus
   * @param id
   * @param name
   * @returns {*}
   */
  opColumn = ({ status, envStatus, id, name }) => {
    const {
      intl: { formatMessage },
      AppState: {
        currentMenuType: {
          type,
          id: projectId,
          organizationId,
        },
      },
    } = this.props;
    let editDom = null;
    let deleteDom = null;
    if (envStatus) {
      if (status !== 'operating') {
        editDom = (
          <Tooltip
            trigger="hover"
            placement="bottom"
            arrowPointAtCenter
            title={<FormattedMessage id="edit" />}
          >
            <Button
              shape="circle"
              size="small"
              funcType="flat"
              icon="mode_edit"
              onClick={this.editNetwork.bind(this, id)}
            />
          </Tooltip>
        );
        deleteDom = (
          <Tooltip
            trigger="hover"
            placement="bottom"
            arrowPointAtCenter
            title={<FormattedMessage id="delete" />}
          >
            <Button
              shape="circle"
              size="small"
              funcType="flat"
              icon="delete_forever"
              onClick={this.openDeleteModal.bind(this, id, name)}
            />
          </Tooltip>
        );
      } else {
        editDom = (
          <Tooltip
            trigger="hover"
            placement="bottomRight"
            arrowPointAtCenter
            title={formatMessage({ id: `network_${status}` })}
          >
            <Button
              disabled
              shape="circle"
              size="small"
              funcType="flat"
              icon="mode_edit"
            />
          </Tooltip>
        );
        deleteDom = (
          <Tooltip
            trigger="hover"
            placement="bottomRight"
            arrowPointAtCenter
            title={formatMessage({ id: `network_${status}` })}
          >
            <Button
              disabled
              shape="circle"
              size="small"
              funcType="flat"
              icon="delete_forever"
            />
          </Tooltip>
        );
      }
    } else {
      editDom = (
        <Tooltip
          trigger="hover"
          placement="bottomRight"
          arrowPointAtCenter
          title={formatMessage({ id: 'network.env.tooltip' })}
        >
          <Button
            disabled
            shape="circle"
            size="small"
            funcType="flat"
            icon="mode_edit"
          />
        </Tooltip>
      );
      deleteDom = (
        <Tooltip
          trigger="hover"
          placement="bottomRight"
          arrowPointAtCenter
          title={formatMessage({ id: 'network.env.tooltip' })}
        >
          <Button
            disabled
            shape="circle"
            size="small"
            funcType="flat"
            icon="delete_forever"
          />
        </Tooltip>
      );
    }

    return (
      <div className="c7ncd-network-operator">
        <Permission
          service={['devops-service.devops-service.update']}
          type={type}
          projectId={projectId}
          organizationId={organizationId}
        >
          {editDom}
        </Permission>
        <Permission
          service={['devops-service.devops-service.delete']}
          type={type}
          projectId={projectId}
          organizationId={organizationId}
        >
          {deleteDom}
        </Permission>
      </div>
    );
  };

  /**
   * 环境选择
   * @param value
   */
  handleEnvSelect = value => {
    EnvOverviewStore.setTpEnvId(value);
    this.loadAllData(1, value);
  };

  render() {
    const {
      NetworkConfigStore,
      intl: { formatMessage },
      AppState: {
        currentMenuType: {
          type,
          id: projectId,
          organizationId,
          name: projectName,
        },
      },
    } = this.props;

    const {
      show,
      showEdit,
      id,
      deleteArr,
      submitting,
    } = this.state;

    const {
      filters,
      paras,
      sort: { columnKey, order },
    } = NetworkConfigStore.getInfo;

    const data = NetworkConfigStore.getAllData;
    const envData = EnvOverviewStore.getEnvcard;
    const envId = EnvOverviewStore.getTpEnvId;

    const envState = envData.length
      ? envData.filter(d => d.id === Number(envId))[0]
      : { connect: false };

    const columns = [
      {
        title: <FormattedMessage id="network.column.name" />,
        key: 'name',
        sorter: true,
        sortOrder: columnKey === 'name' && order,
        filters: [],
        filteredValue: filters.name || [],
        render: record => (
          <StatusIcon
            name={record.name}
            status={record.status || ''}
            error={record.error || ''}
          />
        ),
      },
      {
        title: <FormattedMessage id="network.column.env" />,
        key: 'envName',
        sorter: true,
        sortOrder: columnKey === 'envName' && order,
        filters: [],
        filteredValue: filters.envName || [],
        render: record => (
          <EnvFlag status={record.envStatus} name={record.envName} />
        ),
      },
      {
        title: <FormattedMessage id="network.target.type" />,
        key: 'targetType',
        render: this.targetTypeColumn,
      },
      {
        title: <FormattedMessage id="network.target" />,
        key: 'target',
        render: this.targetColumn,
      },
      {
        width: 108,
        title: <FormattedMessage id="network.config.column" />,
        key: 'type',
        render: this.configColumn,
      },
      {
        width: 82,
        key: 'action',
        render: this.opColumn,
      },
    ];

    let mainContent = null;
    if (envData && envData.length && envId) {
      this.initAutoRefresh('network');
      mainContent = (
        <Fragment>
          <Header title={<FormattedMessage id="network.header.title" />}>
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
              organizationId={organizationId}
            >
              <Tooltip
                title={
                  envState && !envState.connect ? (
                    <FormattedMessage id="envoverview.envinfo" />
                  ) : null
                }
              >
                <Button
                  disabled={envState && !envState.connect}
                  funcType="flat"
                  onClick={this.showSideBar}
                >
                  <i className="icon-playlist_add icon" />
                  <span>
                    <FormattedMessage id="network.header.create" />
                  </span>
                </Button>
              </Tooltip>
            </Permission>
            <Permission
              service={['devops-service.devops-service.listByEnv']}
              type={type}
              projectId={projectId}
              organizationId={organizationId}
            >
              <RefreshBtn name="network" onFresh={this.handleRefresh} />
            </Permission>
          </Header>
          <Content code="network" values={{ name: projectName }}>
            <Table
              filterBarPlaceholder={formatMessage({ id: 'filter' })}
              loading={NetworkConfigStore.getLoading}
              pagination={NetworkConfigStore.getPageInfo}
              columns={columns}
              onChange={this.tableChange}
              dataSource={data}
              rowKey={record => record.id}
              filters={paras.slice()}
            />
          </Content>
        </Fragment>
      );
    } else {
      mainContent = (
        <DepPipelineEmpty
          title={<FormattedMessage id="network.header.title" />}
          type="env"
        />
      );
    }

    const deleteModals = _.map(deleteArr, ({ name, display, deleteId }) => (<DeleteModal
      key={deleteId}
      title={`${formatMessage({ id: 'service.delete' })}“${name}”`}
      visible={display}
      objectId={deleteId}
      loading={submitting}
      objectType="service"
      onClose={this.closeDeleteModal}
      onOk={this.handleDelete}
    />));

    return (
      <Page
        service={[
          'devops-service.devops-service.create',
          'devops-service.devops-service.checkName',
          'devops-service.devops-service.listByEnv',
          'devops-service.devops-service.query',
          'devops-service.devops-service.update',
          'devops-service.devops-service.delete',
          'devops-service.devops-service.listByEnvId',
          'devops-service.devops-environment.listByProjectIdAndActive',
          'devops-service.application.listByEnvIdAndStatus',
          'devops-service.application-version.queryByAppIdAndEnvId',
          'devops-service.application-instance.listByAppVersionId',
        ]}
        className="c7n-region c7n-network-wrapper"
      >
        {NetworkConfigStore.isRefresh ? <LoadingBar display /> : mainContent}

        {show && (
          <CreateNetwork
            envId={envId}
            visible={show}
            store={NetworkConfigStore}
            onClose={this.handleCancelFun}
          />
        )}
        {showEdit && (
          <EditNetwork
            netId={id}
            visible={showEdit}
            store={NetworkConfigStore}
            onClose={this.handleCancelFun}
          />
        )}
        {deleteModals}
      </Page>
    );
  }
}
