import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Table,
  Button,
  Modal,
  Tooltip,
} from 'choerodon-ui';
import {
  Content,
  Header,
  Page,
  Permission,
} from '@choerodon/boot';
import _ from 'lodash';
import TimePopover from '../../../components/timePopover';
import EnvFlag from '../../../components/envFlag';
import DeploymentConfigCreate from '../deploymentConfigCreate';
import UserInfo from '../../../components/userInfo';
import DeploymentPipelineStore from '../../../stores/project/deploymentPipeline';
import DepPipelineEmpty from '../../../components/DepPipelineEmpty/DepPipelineEmpty';

import '../../main.scss';
import './DeploymentConfig.scss';

@injectIntl
@withRouter
@inject('AppState')
@observer
class DeploymentConfig extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
      sidebarType: null,
      id: null,
      name: null,
      deleteLoading: false,
      canDelete: false,
    };
  }

  componentDidMount() {
    const {
      location: { state },
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    const { appId, envId } = state || {};
    appId && envId && this.setState({ sidebarType: 'create' });
    DeploymentPipelineStore.loadActiveEnv(projectId);
    this.loadData();
  }

  loadData = () => {
    const {
      DeploymentConfigStore,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    DeploymentConfigStore.loadAllData(projectId, 1, 10);
  };

  /**
   * 处理刷新函数
   */
  handleRefresh = () => {
    const { DeploymentConfigStore } = this.props;
    const pageInfo = DeploymentConfigStore.getPageInfo;
    const { filters, sort, paras } = DeploymentConfigStore.getInfo;
    this.tableChange(pageInfo, filters, sort, paras);
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
      DeploymentConfigStore,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    DeploymentConfigStore.setInfo({ filters, sort: sorter, paras });
    const sort = { field: 'id', order: 'desc' };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === 'ascend') {
        sort.order = 'asc';
      } else if (sorter.order === 'descend') {
        sort.order = 'desc';
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
    DeploymentConfigStore.loadAllData(
      projectId,
      current,
      pageSize,
      sort,
      postData,
    );
  };

  /**
   * 获取表格行
   */
  getColumns = () => {
    const {
      DeploymentConfigStore,
      intl: { formatMessage },
      AppState: {
        currentMenuType: { projectId, type, organizationId },
      },
    } = this.props;
    const {
      filters,
      sort: { columnKey, order },
    } = DeploymentConfigStore.getInfo;
    return [
      {
        title: formatMessage({ id: 'app.name' }),
        key: 'name',
        dataIndex: 'name',
        sorter: true,
        sortOrder: columnKey === 'name' && order,
        filters: [],
        filteredValue: filters.name || [],
      },
      {
        title: formatMessage({ id: 'template.des' }),
        key: 'description',
        dataIndex: 'description',
        sorter: true,
        sortOrder: columnKey === 'description' && order,
        filters: [],
        filteredValue: filters.description || [],
      },
      {
        title: formatMessage({ id: 'deploy.app' }),
        key: 'appName',
        dataIndex: 'appName',
      },
      {
        title: formatMessage({ id: 'deploy.env' }),
        key: 'envName',
        render: record => (
          <EnvFlag status={record.envStatus} name={record.envName} />
        ),
      },
      {
        title: formatMessage({ id: 'app.creator' }),
        key: 'creator',
        render: ({ createUserUrl, createUserRealName, createUserName }) => (<UserInfo avatar={createUserUrl} name={createUserRealName} id={createUserName} />),
      },
      {
        title: <FormattedMessage id="ist.expand.date" />,
        dataIndex: 'lastUpdateDate',
        key: 'lastUpdateDate',
        render: text => <TimePopover content={text} />,
      },
      {
        key: 'action',
        render: (test, record) => (
          <div>
            <Permission
              type={type}
              projectId={projectId}
              organizationId={organizationId}
              service={['devops-service.pipeline-value.createOrUpdate']}
            >
              <Tooltip
                placement="bottom"
                title={<FormattedMessage id="edit" />}
              >
                <Button
                  icon="mode_edit"
                  shape="circle"
                  size="small"
                  onClick={this.showSidebar.bind(this, 'edit', record.id)}
                />
              </Tooltip>
            </Permission>
            <Permission
              type={type}
              projectId={projectId}
              organizationId={organizationId}
              service={['devops-service.pipeline-value.delete']}
            >
              <Tooltip
                placement="bottom"
                title={<FormattedMessage id="delete" />}
              >
                <Button
                  icon="delete_forever"
                  shape="circle"
                  size="small"
                  onClick={this.showSidebar.bind(this, 'delete', record.id, record.name)}
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
    const {
      DeploymentConfigStore,
      AppState: { currentMenuType: { projectId } },
    } = this.props;
    if (sidebarType === 'delete') {
      DeploymentConfigStore.checkDelete(projectId, id)
        .then((data) => {
          if (data && data.failed) {
            Choerodon.prompt(data.message);
          } else {
            // data 为true表示能删除，false表示不能删除
            this.setState({ canDelete: data, sidebarType, id, name });
          }
        });
    } else {
      this.setState({ sidebarType, id, name });
    }
  };

  /**
   * 关闭弹窗
   */
  handClose = (flag) => {
    if (flag) {
      const { DeploymentConfigStore } = this.props;
      DeploymentConfigStore.setInfo({
        filters: {},
        sort: { columnKey: 'id', order: 'descend' },
        paras: [],
      });
      this.loadData();
    }
    this.setState({ sidebarType: null, id: null, name: null, canDelete: false });
  };

  /**
   * 删除部署配置
   */
  handleDelete = () => {
    const {
      DeploymentConfigStore,
      intl: { formatMessage },
      AppState: {
        currentMenuType: { projectId },
      },
    } = this.props;
    const { id } = this.state;
    this.setState({ deleteLoading: true });
    DeploymentConfigStore.deleteData(projectId, id)
      .then((data) => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.handClose(true);
        }
        this.setState({ deleteLoading: false });
      })
      .catch((error) => {
        this.setState({ deleteLoading: false });
        Choerodon.handleResponseError(error);
      });
  };

  render() {
    const {
      DeploymentConfigStore,
      intl: { formatMessage },
      AppState: {
        currentMenuType: { projectId, type, organizationId: orgId, name },
      },
      location: { state },
    } = this.props;
    const {
      sidebarType,
      name: configName,
      id,
      deleteLoading,
      canDelete,
    } = this.state;

    const { loading, pageInfo } = DeploymentConfigStore;
    const data = DeploymentConfigStore.getConfigList;
    const { paras } = DeploymentConfigStore.getInfo;
    const envNames = _.filter(DeploymentPipelineStore.getEnvLine, ['permission', true]);

    return (
      <Page
        className="c7n-region c7n-deploymentConfig-wrapper"
        service={[
          'devops-service.pipeline-value.listByOptions',
          'devops-service.pipeline-value.queryById',
          'devops-service.pipeline-value.createOrUpdate',
          'devops-service.pipeline-value.delete',
        ]}
      >
        {envNames && envNames.length ? <Fragment>
          <Header title={formatMessage({ id: 'deploymentConfig.header' })}>
            <Permission
              service={['devops-service.pipeline-value.createOrUpdate']}
              organizationId={orgId}
              projectId={projectId}
              type={type}
            >
              <Button
                onClick={this.showSidebar.bind(this, 'create')}
                icon="playlist_add"
              >
                <FormattedMessage id="deploymentConfig.create.header" />
              </Button>
            </Permission>
            <Button
              onClick={this.handleRefresh}
              icon="refresh"
            >
              <FormattedMessage id="refresh" />
            </Button>
          </Header>
          <Content code="deploymentConfig" values={{ name }}>
            <Table
              filterBarPlaceholder={formatMessage({ id: 'filter' })}
              loading={loading}
              onChange={this.tableChange}
              pagination={pageInfo}
              columns={this.getColumns()}
              dataSource={data}
              rowKey={record => record.id}
              filters={paras.slice()}
            />
          </Content>
          {(sidebarType === 'create' || sidebarType === 'edit')
            && <DeploymentConfigCreate
              sidebarType={sidebarType}
              store={DeploymentConfigStore}
              onClose={this.handClose}
              id={id}
              state={state || {}}
            />
          }
          <Modal
            confirmLoading={deleteLoading}
            visible={sidebarType === 'delete'}
            title={`${formatMessage({ id: 'deploymentConfig.delete' })}“${configName}”`}
            closable={false}
            onOk={this.handleDelete}
            onCancel={this.handClose.bind(this, false)}
            okText={formatMessage({ id: 'delete' })}
            okType="danger"
            footer={canDelete ? [
              <Button
                key="back"
                onClick={this.handClose.bind(this, false)}
                disabled={deleteLoading}
              >
                <FormattedMessage id="cancel" />
              </Button>,
              <Button
                key="delete"
                loading={deleteLoading}
                type="danger"
                onClick={this.handleDelete}
              >
                <FormattedMessage id="delete" />
              </Button>,
            ] : [
              <Button
                key="back"
                type="primary"
                onClick={this.handClose.bind(this, false)}
              >
                <FormattedMessage id="pipelineRecord.check.tips.button" />
              </Button>,
            ]}
          >
            <div className="c7n-padding-top_8">
              <FormattedMessage id={canDelete ? 'deploymentConfig.delete.tooltip' : 'deployment.delete.unable'} />
            </div>
          </Modal>
        </Fragment> : (
          <DepPipelineEmpty
            title={<FormattedMessage id="deploymentConfig.header" />}
            type="env"
          />
        )}
      </Page>
    );
  }
}

export default DeploymentConfig;
