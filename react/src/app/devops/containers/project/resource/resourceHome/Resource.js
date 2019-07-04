import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Table,
  Button,
  Modal,
  Tooltip,
  Select,
} from 'choerodon-ui';
import {
  Content,
  Header,
  Page,
  Permission,
} from '@choerodon/boot';
import _ from 'lodash';
import classnames from 'classnames';
import TimePopover from '../../../../components/timePopover';
import StatusIcon from '../../../../components/StatusIcon/StatusIcon';
import EnvOverviewStore from '../../../../stores/project/envOverview';
import ResourceSidebar from '../resourceSidebar/ResourceSidebar';
import ResourceDetail from '../components/resourceDetail/ResourceDetail';
import { handlePromptError } from '../../../../utils';

import '../../../main.scss';

const { Option } = Select;

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class Resource extends Component {
  state = {
    id: null,
    name: null,
    deleteLoading: false,
    sidebarType: null,
  };

  componentDidMount() {
    const {
      AppState: {
        currentMenuType: {
          projectId,
        },
      },
    } = this.props;
    EnvOverviewStore.loadActiveEnv(projectId, 'customResource');
  }

  /**
   * 加载自定义资源列表
   */
  loadData = () => {
    const {
      ResourceStore,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    const envId = EnvOverviewStore.getTpEnvId;
    ResourceStore.loadResource(projectId, envId);
  };

  /**
   * 处理刷新函数
   */
  handleRefresh = () => {
    const { ResourceStore } = this.props;
    const {
      getPageInfo,
      getInfo: {
        filters,
        sort,
        paras,
      },
    } = ResourceStore;
    this.tableChange(getPageInfo, filters, sort, paras);
  };

  /**
   * table 操作
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  tableChange = ({ current, pageSize }, filters, sorter, paras) => {
    const {
      ResourceStore,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    const envId = EnvOverviewStore.getTpEnvId;
    const sort = _.isEmpty(sorter) ? null : sorter;
    const postData = {
      searchParam: filters,
      param: paras.toString(),
    };

    ResourceStore.setInfo({ filters, sort, paras });
    ResourceStore.loadResource(
      projectId,
      envId,
      current,
      pageSize,
      sort,
      postData,
    );
  };

  /**
   * 环境选择
   * @param value
   */
  handleEnvSelect = (value) => {
    EnvOverviewStore.setTpEnvId(value);
    this.loadData();
  };

  /**
   * 获取表格行
   */
  getColumns = () => {
    const {
      ResourceStore,
      intl: { formatMessage },
    } = this.props;
    const {
      filters,
      sort,
    } = ResourceStore.getInfo;
    const { columnKey, order } = sort || {};
    return [
      {
        title: formatMessage({ id: 'resource.type' }),
        key: 'k8s_kind',
        sorter: true,
        sortOrder: columnKey === 'k8s_kind' && order,
        filters: [],
        filteredValue: filters.k8sKind || [],
        render: record => <span>{record.k8sKind}</span>,
      },
      {
        title: formatMessage({ id: 'name' }),
        key: 'name',
        dataIndex: 'name',
        sorter: true,
        sortOrder: columnKey === 'name' && order,
        filters: [],
        filteredValue: filters.name || [],
        render: (text, { commandErrors, name, commandStatus }) => (
          <StatusIcon status={commandStatus} name={name} error={commandErrors} />
        ),
      },
      {
        title: formatMessage({ id: 'updateDate' }),
        dataIndex: 'lastUpdateDate',
        key: 'lastUpdateDate',
        render: text => <TimePopover content={text} />,
      },
      {
        key: 'action',
        width: '100px',
        render: (test, { id, name, envStatus, commandStatus }) => (
          <div>
            <Permission
              service={['devops-service.devops-customize-resource.getResource']}
            >
              <Tooltip
                placement="bottom"
                title={<FormattedMessage id="edit" />}
              >
                <Button
                  icon="find_in_page"
                  shape="circle"
                  size="small"
                  disabled={!envStatus}
                  onClick={this.showSidebar.bind(this, 'view', id)}
                />
              </Tooltip>
            </Permission>
            <Permission
              service={['devops-service.devops-customize-resource.createResource']}
            >
              <Tooltip
                placement="bottom"
                title={<FormattedMessage id="edit" />}
              >
                <Button
                  icon="mode_edit"
                  shape="circle"
                  size="small"
                  disabled={!envStatus || commandStatus === 'operating'}
                  onClick={this.showSidebar.bind(this, 'edit', id)}
                />
              </Tooltip>
            </Permission>
            <Permission
              service={['devops-service.devops-customize-resource.deleteResource']}
            >
              <Tooltip
                placement="bottom"
                title={<FormattedMessage id="delete" />}
              >
                <Button
                  icon="delete_forever"
                  shape="circle"
                  size="small"
                  disabled={!envStatus || commandStatus === 'operating'}
                  onClick={this.showSidebar.bind(this, 'delete', id, name)}
                />
              </Tooltip>
            </Permission>
          </div>
        ),
      },
    ];
  };

  /**
   * 展开弹窗
   */
  showSidebar = (sidebarType, id = null, name = null) => {
    this.setState({ sidebarType, id, name });
  };

  /**
   * 关闭弹窗
   */
  handClose = (flag) => {
    if (flag) {
      const { ResourceStore } = this.props;
      ResourceStore.setInfo({
        filters: {},
        sort: null,
        paras: [],
      });
      this.loadData();
    }
    this.setState({ sidebarType: null, id: null, name: null });
  };

  /**
   * 删除自定义资源
   */
  handleDelete = () => {
    const {
      ResourceStore,
      AppState: {
        currentMenuType: { projectId },
      },
    } = this.props;
    const { id } = this.state;
    this.setState({ deleteLoading: true });
    ResourceStore.deleteData(projectId, id)
      .then((data) => {
        if (handlePromptError(data)) {
          this.handClose(true);
        }
        this.setState({ deleteLoading: false });
      })
      .catch((e) => {
        this.setState({ deleteLoading: false });
        Choerodon.handleResponseError(e);
      });
  };

  render() {
    const {
      ResourceStore,
      intl: { formatMessage },
    } = this.props;
    const {
      sidebarType,
      name,
      id,
      deleteLoading,
    } = this.state;

    const {
      getResourceList,
      getLoading,
      getPageInfo,
      getInfo: {
        paras,
      },
    } = ResourceStore;

    const {
      getEnvcard,
      getTpEnvId,
    } = EnvOverviewStore;
    const envState = _.filter(getEnvcard, { id: getTpEnvId, connect: true });
    const selectClass = classnames({
      'c7n-header-select': true,
      'c7n-select_min100': !getTpEnvId,
    });
    const envOptions = _.map(getEnvcard, ({ connect, id: envId, permission, name: envName }) => {
      const envOptionClass = classnames({
        'c7ncd-status': true,
        'c7ncd-status-success': connect,
        'c7ncd-status-disconnect': !connect,
      });

      return (<Option
        key={envId}
        value={envId}
        disabled={!permission}
        title={envName}
      >
        <Tooltip placement="right" title={envName}>
          <span className="c7n-ib-width_100">
            <span className={envOptionClass} />
            {envName}
          </span>
        </Tooltip>
      </Option>);
    });

    return (
      <Page
        className="c7n-region c7n-deploymentConfig-wrapper"
        service={[
          'devops-service.devops-customize-resource.pageByEnv',
          'devops-service.devops-customize-resource.createResource',
          'devops-service.devops-customize-resource.deleteResource',
          'devops-service.devops-customize-resource.getResource',
        ]}
      >
        <Header title={formatMessage({ id: 'resource.header' })}>
          <Select
            className={selectClass}
            dropdownClassName="c7n-header-env_drop"
            placeholder={formatMessage({ id: 'envoverview.noEnv' })}
            value={getTpEnvId || undefined}
            disabled={getEnvcard && getEnvcard.length === 0}
            onChange={this.handleEnvSelect}
          >
            {envOptions}
          </Select>
          <Permission
            service={['devops-service.devops-customize-resource.createResource']}
          >
            <Tooltip title={formatMessage({ id: 'envoverview.envinfo' })}>
              <Button
                onClick={this.showSidebar.bind(this, 'create')}
                disabled={!(envState && envState.length)}
                icon="playlist_add"
              >
                <FormattedMessage id="resource.create.header" />
              </Button>
            </Tooltip>
          </Permission>
          <Button
            onClick={this.handleRefresh}
            icon="refresh"
          >
            <FormattedMessage id="refresh" />
          </Button>
        </Header>
        <Content code="resource">
          <Table
            filterBarPlaceholder={formatMessage({ id: 'filter' })}
            loading={getLoading}
            onChange={this.tableChange}
            pagination={getPageInfo}
            columns={this.getColumns()}
            dataSource={getResourceList}
            rowKey={record => record.id}
            filters={paras.slice()}
          />
        </Content>
        {sidebarType === 'delete' && (
          <Modal
            confirmLoading={deleteLoading}
            visible={sidebarType === 'delete'}
            title={formatMessage({ id: 'resource.delete.header' }, { name })}
            closable={false}
            onOk={this.handleDelete}
            onCancel={this.handClose.bind(this, false)}
            okText={formatMessage({ id: 'delete' })}
            okType="danger"
          >
            <div className="c7n-padding-top_8">
              <FormattedMessage id="resource.delete.tips" />
            </div>
          </Modal>
        )}
        {(sidebarType === 'create' || sidebarType === 'edit') && (
          <ResourceSidebar
            id={id}
            envId={getTpEnvId}
            type={sidebarType}
            store={ResourceStore}
            visible={sidebarType === 'create' || sidebarType === 'edit'}
            onClose={this.handClose}
          />
        )}
        {sidebarType === 'view' && (
          <ResourceDetail
            id={id}
            store={ResourceStore}
            visible={sidebarType === 'view'}
            onClose={this.handClose}
          />
        )}
      </Page>
    );
  }
}
