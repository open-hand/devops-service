/* eslint-disable react/sort-comp */
import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { observable, action } from 'mobx';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Table,
  Button,
  Form,
  Tooltip,
  Popover,
  Icon,
} from 'choerodon-ui';
import { Permission } from '@choerodon/boot';
import _ from 'lodash';
import NetworkConfigStore from '../../../../stores/project/networkConfig';
import EditNetwork from '../../networkConfig/editNetwork';
import StatusIcon from '../../../../components/StatusIcon';
import DeleteModal from '../../../../components/deleteModal';
import { handleProptError } from '../../../../utils';

import '../EnvOverview.scss';
import '../../../main.scss';
import '../../networkConfig/networkHome/NetworkHome.scss';

@Form.create({})
@withRouter
@injectIntl
@inject('AppState')
@observer
export default class NetworkOverview extends Component {
  @observable showEdit = false;

  state = {
    deleteArr: [],
    deleteLoading: false,
  };

  /**
   * 打开编辑的操作框
   * @param id
   */
  @action
  editNetwork = id => {
    NetworkConfigStore.setApp([]);
    NetworkConfigStore.setEnv([]);
    NetworkConfigStore.setIst([]);
    this.showEdit = true;
    this.id = id;
  };

  loadNetwork = (envId, spin = true) => {
    const {
      store,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    store.loadNetwork(spin, projectId, envId);
  };

  /**
   * 关闭侧边栏
   */
  @action
  handleCancelFun = isload => {
    this.showEdit = false;
    if (isload) {
      this.loadNetwork(this.props.envId);
      const { store } = this.props;
      store.setInfo({
        filters: {},
        sort: { columnKey: 'id', order: 'descend' },
        paras: [],
      });
    }
  };

  /**
   * 配置类型 列
   * @param record
   * @returns {Array}
   */
  configColumn = record => {
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
            {nodePort} {port} {targetPort}
          </div>,
        );
      });
    }
    const content =
      type === 'NodePort' ? (
        <Fragment>
          <div className="network-config-item">
            <FormattedMessage id="network.node.port" />
          </div>
          <div>{portArr}</div>
        </Fragment>
      ) : (
        <Fragment>
          {type === 'ClusterIP' && (
            <div className="network-config-wrap">
              <div className="network-type-title">
                <FormattedMessage id="network.column.ip" />
              </div>
              <div>{externalIps ? iPArr : '-'}</div>
            </div>
          )}
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

    const message = {
      instance: formatMessage({ id: 'ist.head' }),
      param: formatMessage({ id: 'branch.issue.label' }),
      endPoints: 'EndPoints',
    };

    let targetType = 'endPoints';
    if (appId && appInstance && appInstance.length) {
      targetType = 'instance';
    } else if (labels) {
      targetType = 'param';
    }

    return <span>{message[targetType]}</span>;
  };

  /**
   * 生成 目标对象 列
   * @param record
   * @returns {Array}
   */
  targetColumn = record => {
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
   * @returns {*}
   */
  renderActions = ({ status, envStatus, id, name }) => {
    const {
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
    if (status !== 'operating' && envStatus) {
      editDom = (
        <Tooltip
          trigger="hover"
          placement="bottom"
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
      editDom = <Button
        disabled
        shape="circle"
        size="small"
        funcType="flat"
        icon="mode_edit"
      />;
      deleteDom = <Button
        disabled
        shape="circle"
        size="small"
        funcType="flat"
        icon="delete_forever"
      />;
    }
    return (
      <Fragment>
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
      </Fragment>
    );
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
        currentMenuType: { id: projectId },
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
    const page = pagination.current - 1;
    if (Object.keys(filters).length) {
      searchParam = filters;
    }
    const postData = {
      searchParam,
      param: paras.toString(),
    };
    store.setInfo({ filters, sort: sorter, paras });
    store.loadNetwork(
      true,
      projectId,
      envId,
      page,
      pagination.pageSize,
      sort,
      postData,
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

    const response = await NetworkConfigStore.deleteData(projectId, id)
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

      store.loadNetwork(
        true,
        projectId,
        envId,
        isLastItem ? current - 2 : current - 1,
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
      store,
      intl: {
        formatMessage,
      },
    } = this.props;
    const { deleteArr, deleteLoading } = this.state;
    const data = store.getNetwork;
    const {
      filters,
      sort: { columnKey, order },
      paras,
    } = store.getInfo;

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
            status={record.commandStatus || ''}
            error={record.error || ''}
          />
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
        render: record => this.targetColumn(record),
      },
      {
        title: <FormattedMessage id="network.config.column" />,
        key: 'type',
        render: record => this.configColumn(record),
      },
      {
        width: '82px',
        key: 'action',
        render: this.renderActions,
      },
    ];

    const deleteModals = _.map(deleteArr, ({ name, display, deleteId }) => (<DeleteModal
      key={deleteId}
      title={`${formatMessage({ id: 'service.delete' })}“${name}”`}
      visible={display}
      objectId={deleteId}
      loading={deleteLoading}
      objectType="service"
      onClose={this.closeDeleteModal}
      onOk={this.handleDelete}
    />));

    return (
      <div className="c7n-network-wrapper">
        <Table
          filterBarPlaceholder={formatMessage({ id: 'filter' })}
          loading={store.isLoading}
          pagination={store.pageInfo}
          columns={columns}
          onChange={this.tableChange}
          dataSource={data ? data.slice() : []}
          rowKey={record => record.id}
          filters={paras.slice()}
        />
        {this.showEdit && (
          <EditNetwork
            netId={this.id}
            visible={this.showEdit}
            store={NetworkConfigStore}
            onClose={this.handleCancelFun}
          />
        )}
        {deleteModals}
      </div>
    );
  }
}
