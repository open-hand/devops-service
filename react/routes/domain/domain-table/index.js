import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Table, Button, Form, Tooltip, Select } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Content,
  Header,
  Page,
  Permission,
} from '@choerodon/boot';
import _ from 'lodash';
import CreateDomain from '../domain-create';
import LoadingBar from '../../../components/loadingBar';
import { commonComponent } from '../../../components/commonFunction';
import StatusIcon from '../../../components/StatusIcon';
import MouserOverWrapper from '../../../components/MouseOverWrapper/MouserOverWrapper';
import EnvOverviewStore from '../../envOverview/stores';
import DepPipelineEmpty from '../../../components/DepPipelineEmpty/DepPipelineEmpty';
import RefreshBtn from '../../../components/refreshBtn';
import StatusTags from '../../../components/StatusTags';
import EnvFlag from '../../../components/envFlag';
import DeleteModal from '../../../components/deleteModal';

import './index.scss';
import '../../main.scss';

const { Option } = Select;

@Form.create({})
@withRouter
@injectIntl
@inject('AppState')
@commonComponent('DomainStore')
@observer
export default class DomainHome extends Component {
  state = {
    submitting: false,
    deleteArr: [],
    show: false,
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
    const { DomainStore } = this.props;
    this.clearAutoRefresh();
    this.clearFilterInfo();
    DomainStore.setAllData([]);
  }

  /**
   * 关闭侧边栏
   */
  handleCancelFun = isload => {
    const { DomainStore } = this.props;
    this.props.form.resetFields();
    this.setState({ show: false, id: null });
    if (isload) {
      DomainStore.setInfo({
        filters: {},
        sort: { columnKey: 'id', order: 'descend' },
        paras: [],
      });
      const envId = EnvOverviewStore.getTpEnvId;
      this.loadAllData(1, envId);
    }
  };

  /**
   *打开域名创建弹框
   */
  showSideBar = (type, id = '') => {
    const { form, DomainStore } = this.props;
    form.resetFields();
    DomainStore.setCertificates([]);
    if (type === 'create') {
      this.setState({
        show: true,
        title: this.props.intl.formatMessage({ id: 'domain.header.create' }),
        type,
        id,
      });
    } else {
      this.setState(
        {
          title: this.props.intl.formatMessage({ id: 'domain.header.update' }),
          type,
          id,
        },
        () => {
          this.setState({ show: true });
        },
      );
    }
  };

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
    switch (status) {
      case 'operating':
        editDom = (
          <Tooltip
            trigger="hover"
            placement="bottomRight"
            arrowPointAtCenter
            title={formatMessage({ id: `domain_${status}` })}
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
            title={formatMessage({ id: `domain_${status}` })}
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
        break;
      default:
        editDom = envStatus ? (
          <Tooltip
            trigger="hover"
            placement="bottom"
            arrowPointAtCenter
            title={<div>{formatMessage({ id: 'edit' })}</div>}
          >
            <Button
              shape="circle"
              size="small"
              funcType="flat"
              icon="mode_edit"
              onClick={this.showSideBar.bind(this, 'edit', id)}
            />
          </Tooltip>
        ) : (
          <Tooltip
            trigger="hover"
            placement="bottomRight"
            arrowPointAtCenter
            title={
              <div>{formatMessage({ id: 'network.env.tooltip' })}</div>
            }
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
        deleteDom = envStatus ? (
          <Tooltip
            trigger="hover"
            placement="bottom"
            arrowPointAtCenter
            title={<div>{formatMessage({ id: 'delete' })}</div>}
          >
            <Button
              shape="circle"
              size="small"
              funcType="flat"
              icon="delete_forever"
              onClick={this.openDeleteModal.bind(this, id, name)}
            />
          </Tooltip>
        ) : (
          <Tooltip
            trigger="hover"
            placement="bottomRight"
            arrowPointAtCenter
            title={
              <div>{formatMessage({ id: 'network.env.tooltip' })}</div>
            }
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
      <Fragment>
        <Permission
          service={['devops-service.devops-ingress.update']}
          type={type}
          projectId={projectId}
          organizationId={organizationId}
        >
          {editDom}
        </Permission>
        <Permission
          service={['devops-service.devops-ingress.delete']}
          type={type}
          projectId={projectId}
          organizationId={organizationId}
        >
          {deleteDom}
        </Permission>
      </Fragment>
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
      DomainStore,
      intl: { formatMessage },
      AppState: {
        currentMenuType: {
          type,
          id: projectId,
          organizationId,
          name,
        },
      },
    } = this.props;

    const {
      deleteArr,
      show,
      id,
      title,
      type: sidebarType,
      submitting,
    } = this.state;

    const data = DomainStore.getAllData;
    const envData = EnvOverviewStore.getEnvcard;
    const envId = EnvOverviewStore.getTpEnvId;

    const envState = envData.length
      ? envData.filter(d => d.id === Number(envId))[0]
      : { connect: false };
    const {
      filters,
      paras,
      sort: { columnKey, order },
    } = DomainStore.getInfo;

    const columns = [
      {
        title: formatMessage({ id: 'domain.column.name' }),
        key: 'name',
        dataIndex: 'name',
        sorter: true,
        sortOrder: columnKey === 'name' && order,
        filters: [],
        filteredValue: filters.name || [],
        render: (text, record) => (
          <div className="c7n-network-service">
            <MouserOverWrapper text={text} width={0.16}>
              {text}
            </MouserOverWrapper>
            <StatusIcon
              name=""
              status={record.commandStatus || ''}
              error={record.error || ''}
            />
          </div>
        ),
      },
      {
        title: formatMessage({ id: 'domain.column.domain' }),
        key: 'domain',
        filters: [],
        filteredValue: filters.domain || [],
        dataIndex: 'domain',
      },
      {
        title: formatMessage({ id: 'domain.column.env' }),
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
        title: formatMessage({ id: 'domain.column.path' }),
        className: 'c7n-network-col',
        key: 'path',
        filters: [],
        filteredValue: filters.path || [],
        render: record =>
          _.map(record.pathList, router => (
            <div
              className="c7n-network-col_border"
              key={`${record.id}-${router.path}`}
            >
              <span>{router.path}</span>
            </div>
          )),
      },
      {
        title: formatMessage({ id: 'domain.column.network' }),
        className: 'c7n-network-col',
        key: 'serviceName',
        filters: [],
        filteredValue: filters.serviceName || [],
        render: record => _.map(record.pathList, ({ serviceStatus, serviceName }) =>
          <div
            className="c7n-network-service"
            key={record.id}
          >
            <StatusTags
              colorCode={serviceStatus}
              name={formatMessage({ id: serviceStatus })}
              style={{
                minWidth: 40,
                marginRight: 8,
              }}
            />
            <MouserOverWrapper text={serviceName} width={0.1}>
              {serviceName}
            </MouserOverWrapper>
          </div>),
      },
      {
        key: 'action',
        align: 'right',
        className: 'c7n-network-text_top',
        render: this.opColumn,
      },
    ];

    let mainContent = null;
    if (envData && envData.length && envId) {
      this.initAutoRefresh('domain');
      mainContent = (
        <Fragment>
          <Header title={formatMessage({ id: 'domain.header.title' })}>
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
              service={['devops-service.devops-ingress.create']}
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
                  icon="playlist_add"
                  funcType="flat"
                  disabled={envState && !envState.connect}
                  onClick={this.showSideBar.bind(this, 'create', '')}
                >
                  <FormattedMessage id="domain.header.create" />
                </Button>
              </Tooltip>
            </Permission>
            <Permission
              service={['devops-service.devops-ingress.listByEnv']}
              type={type}
              projectId={projectId}
              organizationId={organizationId}
            >
              <RefreshBtn name="domain" onFresh={this.handleRefresh} />
            </Permission>
          </Header>
          <Content code="domain" values={{ name }}>
            <Table
              filterBarPlaceholder={formatMessage({ id: 'filter' })}
              loading={DomainStore.loading}
              onChange={this.tableChange}
              pagination={DomainStore.pageInfo}
              columns={columns}
              dataSource={data}
              noFilter
              rowKey={record => record.id}
              filters={paras.slice()}
            />
          </Content>
        </Fragment>
      );
    } else {
      mainContent = (
        <DepPipelineEmpty
          title={formatMessage({ id: 'domain.header.title' })}
          type="env"
        />
      );
    }

    const deleteModals = _.map(deleteArr, ({ name, display, deleteId }) => (<DeleteModal
      key={deleteId}
      title={`${formatMessage({ id: 'ingress.delete' })}“${name}”`}
      visible={display}
      objectId={deleteId}
      loading={submitting}
      objectType="ingress"
      onClose={this.closeDeleteModal}
      onOk={this.handleDelete}
    />));

    return (
      <Page
        className="c7n-region c7n-domain-wrapper"
        service={[
          'devops-service.devops-ingress.create',
          'devops-service.devops-ingress.checkDomain',
          'devops-service.devops-ingress.checkName',
          'devops-service.devops-ingress.listByEnv',
          'devops-service.devops-ingress.queryDomainId',
          'devops-service.devops-ingress.update',
          'devops-service.devops-ingress.delete',
          'devops-service.devops-service.listByEnvId',
          'devops-service.devops-environment.listByProjectIdAndActive',
        ]}
      >
        {DomainStore.isRefresh ? <LoadingBar display /> : mainContent}
        {show && (
          <CreateDomain
            id={id}
            envId={envId}
            title={title}
            visible={show}
            type={sidebarType}
            store={DomainStore}
            onClose={this.handleCancelFun}
          />
        )}
        {deleteModals}
      </Page>
    );
  }
}
