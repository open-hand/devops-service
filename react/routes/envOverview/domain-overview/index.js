/* eslint-disable react/sort-comp */
import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { observable, action } from 'mobx';
import { withRouter } from 'react-router-dom';
import { injectIntl } from 'react-intl';
import { Table, Button, Form, Tooltip } from 'choerodon-ui';
import { Permission } from '@choerodon/boot';
import _ from 'lodash';
import DomainStore from '../../domain/stores';
import CreateDomain from '../../domain/domain-create';
import StatusIcon from '../../../components/StatusIcon';
import { handleProptError } from '../../../utils';
import DeleteModal from '../../../components/deleteModal';

import '../index.scss';
import '../../domain/domain-table/index.scss';
import '../../main.scss';

@Form.create({})
@withRouter
@injectIntl
@inject('AppState')
@observer
export default class DomainOverview extends Component {
  @observable showDomain = false;

  state = {
    deleteArr: [],
    deleteLoading: false,
  };

  /**
   *
   * @param envId
   * @param spin
   */
  loadDomain = (envId, spin = true) => {
    const {
      store,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    store.loadDomain(spin, projectId, envId);
  };

  /**
   *打开域名编辑弹框
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
   * 关闭域名侧边栏
   */
  @action
  closeDomain = isload => {
    this.props.form.resetFields();
    this.showDomain = false;
    this.domainId = null;
    if (isload) {
      this.loadDomain(this.props.envId);
      const { store } = this.props;
      store.setInfo({
        filters: {},
        sort: { columnKey: 'id', order: 'descend' },
        paras: [],
      });
    }
  };

  /**
   * table 操作
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  tableChange = (pagination, filters, sorter, paras) => {
    const {
      store,
      envId,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    const sort = { field: '', order: 'desc' };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === 'ascend') {
        sort.order = 'asc';
      } else if (sorter.order === 'descend') {
        sort.order = 'desc';
      }
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
    store.setInfo({ filters, sort: sorter, paras });
    store.loadDomain(
      true,
      projectId,
      envId,
      page,
      pagination.pageSize,
      sort,
      postData,
    );
  };

  renderActions = ({ status, envStatus, id, name }) => {
    const {
      intl: {
        formatMessage,
      },
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
            arrowPointAtCenter
            placement="bottomRight"
            title={formatMessage({ id: `domain_${status}` })}
          >
            <Button
              disabled
              icon="mode_edit"
              shape="circle"
              size="small"
              funcType="flat"
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
        editDom = (
          <React.Fragment>
            {envStatus ? (
              <Tooltip
                trigger="hover"
                placement="bottom"
                title={<div>{formatMessage({ id: 'edit' })}</div>}
              >
                <Button
                  icon="mode_edit"
                  shape="circle"
                  size="small"
                  funcType="flat"
                  onClick={this.createDomain.bind(this, 'edit', id)}
                />
              </Tooltip>
            ) : (
              <Tooltip
                trigger="hover"
                arrowPointAtCenter
                placement="bottomRight"
                title={
                  <div>
                    {formatMessage({ id: 'network.env.tooltip' })}
                  </div>
                }
              >
                <Button
                  disabled
                  icon="mode_edit"
                  shape="circle"
                  size="small"
                  funcType="flat"
                />
              </Tooltip>
            )}
          </React.Fragment>
        );
        deleteDom = (
          <React.Fragment>
            {envStatus ? (
              <Tooltip
                trigger="hover"
                placement="bottom"
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
                arrowPointAtCenter
                placement="bottomRight"
                title={
                  <div>
                    {formatMessage({ id: 'network.env.tooltip' })}
                  </div>
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
            )}
          </React.Fragment>
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

  handleDelete = async (id, callback) => {
    const {
      store,
      envId,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;

    this.setState({ deleteLoading: true });

    const response = await DomainStore.deleteData(projectId, id)
      .catch(error => {
        this.setState({ deleteLoading: false });
        callback && callback();
        Choerodon.handleResponseError(error);
      });

    const result = handleProptError(response);

    if (result) {
      this.removeDeleteModal(id);

      const { total, current, pageSize } = store.getPageInfo;
      const lastData = total % pageSize;
      const totalPage = Math.ceil(total / pageSize);
      const isLastItem = lastData === 1 && current === totalPage && current > 1;

      store.loadDomain(
        true,
        projectId,
        envId,
        isLastItem ? current - 1 : current,
      );
      store.setInfo({
        filters: {},
        sort: { columnKey: 'id', order: 'descend' },
        paras: [],
      });
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

  render() {
    const {
      intl: {
        formatMessage,
      },
      store,
      envId,
    } = this.props;
    const {
      deleteArr,
      deleteLoading,
    } = this.state;

    const data = store.getDomain;

    const {
      filters,
      sort: { columnKey, order },
      paras,
    } = store.getInfo;

    const columns = [
      {
        title: formatMessage({ id: 'domain.column.name' }),
        key: 'name',
        sorter: true,
        sortOrder: columnKey === 'name' && order,
        filters: [],
        filteredValue: filters.name || [],
        render: record => (
          <StatusIcon
            name={record.name}
            status={record.commandStatus || ''}
            error={record.error || ''}
          />
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
        title: formatMessage({ id: 'domain.column.path' }),
        className: 'c7n-network-col',
        key: 'path',
        sorter: true,
        sortOrder: columnKey === 'path' && order,
        filters: [],
        filteredValue: filters.path || [],
        render: record => (
          <div>
            {_.map(record.pathList, router => (
              <div className="c7n-network-col_border" key={router.path}>
                <span>{router.path}</span>
              </div>
            ))}
          </div>
        ),
      },
      {
        title: formatMessage({ id: 'domain.column.network' }),
        className: 'c7n-network-col',
        key: 'serviceName',
        filters: [],
        filteredValue: filters.serviceName || [],
        render: record => (
          <div>
            {_.map(record.pathList, instance => (
              <div
                className="c7n-network-col_border"
                key={`${instance.path}-${instance.serviceId}`}
              >
                <Tooltip
                  title={formatMessage({
                    id: `${instance.serviceStatus || 'null'}`,
                  })}
                  placement="top"
                >
                  <span
                    className={`c7ncd-status c7ncd-status-${
                      instance.serviceStatus === 'running'
                        ? 'success'
                        : 'disconnect'
                      }`}
                  />
                </Tooltip>
                {instance.serviceName}
              </div>
            ))}
          </div>
        ),
      },
      {
        key: 'action',
        align: 'right',
        className: 'c7n-network-text_top',
        render: this.renderActions,
      },
    ];

    const deleteModals = _.map(deleteArr, ({ name, display, deleteId }) => (<DeleteModal
      key={deleteId}
      title={`${formatMessage({ id: 'ingress.delete' })}“${name}”`}
      visible={display}
      objectId={deleteId}
      loading={deleteLoading}
      objectType="ingress"
      onClose={this.closeDeleteModal}
      onOk={this.handleDelete}
    />));

    return (
      <div className="c7n-domain-wrapper">
        <Table
          filterBarPlaceholder={formatMessage({ id: 'filter' })}
          loading={store.isLoading}
          onChange={this.tableChange}
          pagination={store.pageInfo}
          columns={columns}
          dataSource={data ? data.slice() : []}
          rowKey={record => record.id}
          filters={paras.slice()}
        />
        {this.showDomain && (
          <CreateDomain
            id={this.domainId}
            title={this.domainTitle}
            envId={envId}
            visible={this.showDomain}
            type={this.domainType}
            store={DomainStore}
            onClose={this.closeDomain}
          />
        )}
        {deleteModals}
      </div>
    );
  }
}
