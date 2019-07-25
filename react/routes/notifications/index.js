/**
 * @author ale0720@163.com
 * @date 2019-05-13 13:23
 */
import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import {
  Permission,
  Content,
  Header,
  Page,
} from '@choerodon/boot';
import { Table, Button, Modal, Tooltip, Select } from 'choerodon-ui';
import _ from 'lodash';
import NotificationSidebar from './NotificationSidebar';
import UserList from './components/userList';
import TableTags from './components/tableTags';
import { handleCheckerProptError } from '../../utils';
import { EVENT, METHOD_OPTIONS, TARGET_OPTIONS } from "./Constants";
import NotificationsStore from './stores';

import './style/Notifications.scss';

const { Option } = Select;

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class Notifications extends Component {
  state = {
    page: 1,
    pageSize: NaN,
    param: '',
    filters: {},
    sorter: null,
    showSidebar: false,
    sidebarType: 'create',
    editId: undefined,
    showDelete: false,
    deleteId: undefined,
    envId: null,
  };

  componentDidMount() {
    this.loadData();
    this.loadEnvironments();
  }

  tableChange = ({ current, pageSize }, filters, sorter, param) => {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const { envId } = this.state;

    const realSorter = _.isEmpty(sorter) ? null : sorter;

    this.setState({
      page: current,
      pageSize,
      param,
      filters,
      sorter: realSorter,
    });

    NotificationsStore.loadListData({
      projectId,
      page: current,
      size: pageSize,
      sort: realSorter,
      param: {
        searchParam: filters,
        param: param.toString(),
      },
      env: envId,
    });
  };

  openCreate = () => {
    this.setState({
      showSidebar: true,
    });
  };

  openEdit(id) {
    this.setState({
      showSidebar: true,
      sidebarType: 'edit',
      editId: id,
    });
  };

  closeSidebar = (reload) => {
    this.setState({
      showSidebar: false,
      sidebarType: 'create',
    });
    reload ? this.loadData(1) : this.loadData();
  };

  handleRefresh = (e, page) => {
    this.loadData(page);
  };

  openRemove(id) {
    this.setState({
      showDelete: true,
      deleteId: id,
    });
  }

  closeRemove = () => {
    this.setState({
      deleteId: undefined,
      showDelete: false,
    });
  };

  handleDelete = async () => {
    const {
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    const { deleteId } = this.state;

    this.setState({ deleteLoading: true });

    const response = await NotificationsStore.deletePipeline(projectId, deleteId)
      .catch(e => {
        this.setState({ deleteLoading: false });
        Choerodon.handleResponseError(e);
      });

    if (handleCheckerProptError(response)) {
      this.closeRemove();
      this.handleRefresh(null, 0);
    }

    this.setState({ deleteLoading: false });
  };

  loadEnvironments() {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    NotificationsStore.loadEnvironments(projectId);
  }

  loadData(toPage) {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const { page, pageSize, param, filters, sorter, envId } = this.state;
    const currentPage = toPage || page;
    const {
      getPageInfo: {
        pageSize: storePageSize,
      },
    } = NotificationsStore;

    const postData = {
      projectId,
      page: currentPage,
      size: pageSize || storePageSize,
      sort: sorter,
      param: {
        searchParam: filters,
        param: param.toString(),
      },
      env: envId,
    };

    NotificationsStore.loadListData(postData);
  }

  handleSelectEnv = (value) => {
    this.setState({ envId: value }, () => this.loadData(1));
  };

  renderUser = ({ userRelDTOS, notifyObject }) => (
    <div className="c7n-devops-userlist-warp">
      <UserList
        type={notifyObject}
        dataSource={userRelDTOS}
      />
    </div>
  );

  /**
   * 事件和通知方式使用相同的组件
   * @param data
   * @param type
   * @returns {*}
   */
  renderTags(data, type) {
    return _.map(data, item => {
      const { intl: { formatMessage } } = this.props;
      return <TableTags
        key={item}
        value={formatMessage({ id: `notification.${type}.${item}` })}
      />;
    });
  }

  renderEvent = ({ notifyTriggerEvent }) => {
    return this.renderTags(notifyTriggerEvent, 'event');
  };

  renderMethod = ({ notifyType }) => {
    return this.renderTags(notifyType, 'method');
  };

  renderAction = ({ id }) => {
    const {
      AppState: {
        currentMenuType: {
          type,
          id: projectId,
          organizationId,
        },
      },
    } = this.props;
    return <Fragment>
      <Permission
        service={['devops-service.devops-notification.update']}
        type={type}
        projectId={projectId}
        organizationId={organizationId}
      >
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
            onClick={this.openEdit.bind(this, id)}
          />
        </Tooltip>
      </Permission>
      <Permission
        service={['devops-service.devops-notification.delete']}
        type={type}
        projectId={projectId}
        organizationId={organizationId}
      >
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
            onClick={this.openRemove.bind(this, id)}
          />
        </Tooltip>
      </Permission>
    </Fragment>;
  };

  get getColumns() {
    const { filters, sorter } = this.state;
    const { columnKey, order } = sorter || {};
    const { intl: { formatMessage } } = this.props;

    return [{
      title: <FormattedMessage id="environment" />,
      key: 'envName',
      dataIndex: 'envName',
      sorter: true,
      sortOrder: columnKey === 'envName' && order,
    }, {
      title: <FormattedMessage id="notification.event" />,
      key: 'notifyTriggerEvent',
      filters: _.map(EVENT, item => ({
        text: formatMessage({ id: `notification.event.${item}` }),
        value: item,
      })),
      filteredValue: filters.notifyTriggerEvent || [],
      render: this.renderEvent,
    }, {
      title: <FormattedMessage id="notification.method" />,
      key: 'notifyType',
      filters: _.map(METHOD_OPTIONS, item => ({
        text: formatMessage({ id: `notification.method.${item}` }),
        value: item,
      })),
      filteredValue: filters.notifyType || [],
      render: this.renderMethod,
    }, {
      title: <FormattedMessage id="notification.target" />,
      key: 'notifyObject',
      filters: _.map(TARGET_OPTIONS, item => ({
        text: formatMessage({ id: `notification.target.${item}` }),
        value: item,
      })),
      filteredValue: filters.notifyObject || [],
      render: this.renderUser,
    }, {
      key: 'action',
      align: 'right',
      width: 88,
      render: this.renderAction,
    }];
  };

  render() {
    const {
      AppState: {
        currentMenuType: {
          name,
          type,
          id: projectId,
          organizationId,
        },
      },
      intl: { formatMessage },
    } = this.props;
    const {
      getLoading,
      getPageInfo,
      getListData,
      getEnvs,
      getEnvLoading,
    } = NotificationsStore;
    const {
      param,
      showDelete,
      deleteLoading,
      showSidebar,
      sidebarType,
      editId,
    } = this.state;

    const envOptions = _.map(getEnvs, ({ id, name }) => (<Option key={id} value={id}>{name}</Option>));

    return (
      <Page
        className="c7n-devops-notifications"
        service={[
          'devops-service.devops-notification.create',
          'devops-service.devops-notification.update',
          'devops-service.devops-notification.check',
          'devops-service.devops-notification.listByOptions',
          'devops-service.devops-notification.queryById',
          'devops-service.devops-notification.delete',
        ]}
      >
        <Header title={<FormattedMessage id="notification.header.title" />}>
          <Permission
            service={['devops-service.devops-notification.create']}
            type={type}
            projectId={projectId}
            organizationId={organizationId}
          >
            <Button
              funcType="flat"
              icon="playlist_add"
              onClick={this.openCreate}
            >
              <FormattedMessage id="notification.header.create" />
            </Button>
          </Permission>
          <Button
            icon='refresh'
            onClick={this.handleRefresh}
          >
            <FormattedMessage id="refresh" />
          </Button>
        </Header>
        <Content code="notification" values={{ name }}>
          <Select
            label={formatMessage({ id: 'notification.env.select' })}
            className="notifications-select"
            optionFilterProp="children"
            onChange={this.handleSelectEnv}
            loading={getEnvLoading}
            filter
            allowClear
            filterOption={(input, option) =>
              option.props.children
                .toLowerCase()
                .indexOf(input.toLowerCase()) >= 0
            }
          >
            {envOptions}
          </Select>
          <Table
            filterBarPlaceholder={formatMessage({ id: 'filter' })}
            loading={getLoading}
            filters={param || []}
            onChange={this.tableChange}
            columns={this.getColumns}
            pagination={getPageInfo}
            dataSource={getListData}
            rowKey={record => record.id}
          />
        </Content>
        {showDelete && (<Modal
          visible={showDelete}
          title={<FormattedMessage id="notification.delete" />}
          closable={false}
          footer={[
            <Button key="back" onClick={this.closeRemove} disabled={deleteLoading}>
              <FormattedMessage id="cancel" />
            </Button>,
            <Button
              key="submit"
              type="danger"
              onClick={this.handleDelete}
              loading={deleteLoading}
            >
              <FormattedMessage id="delete" />
            </Button>,
          ]}
        >
          <div className="c7n-padding-top_8">
            <FormattedMessage id="notification.delete.message" />
          </div>
        </Modal>)}
        {showSidebar && <NotificationSidebar
          type={sidebarType}
          visible={showSidebar}
          id={editId}
          store={NotificationsStore}
          onClose={this.closeSidebar}
        />}
      </Page>
    );
  }
}
