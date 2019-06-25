import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Table, Button, Modal, Spin, Select } from 'choerodon-ui';
import { Permission, Content, Header, Page, Action } from '@choerodon/boot';
import _ from 'lodash';
import StatusTags from '../../../../components/StatusTags';
import TimePopover from '../../../../components/timePopover';
import UserInfo from '../../../../components/userInfo';
import { handleCheckerProptError } from '../../../../utils';
import { FAST_SEARCH } from '../components/Constants';

import './Pipeline.scss';

const { Option } = Select;

const STATUS_INVALID = 0;
const STATUS_ACTIVE = 1;
const EXECUTE_PASS = 'pass';
const EXECUTE_FAILED = 'failed';

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class Pipeline extends Component {
  state = {
    page: 1,
    pageSize: NaN,
    param: '',
    filters: {},
    sorter: null,
    showDelete: false,
    deleteName: '',
    deleteId: null,
    deleteLoading: false,
    showInvalid: false,
    invalidName: '',
    invalidId: null,
    invalidLoading: false,
    showExecute: false,
    executeId: null,
    executeName: '',
    executeCheck: false,
    executeLoading: false,
    executeEnv: null,
    searchData: null,
    envIds: null,
  };

  componentDidMount() {
    this.loadAllData();
  }

  handleRefresh = (e, page) => {
    this.loadAllData(page);
  };

  loadAllData = (page) => {
    const {
      PipelineStore,
      AppState: {
        currentMenuType: {
          projectId,
        },
      },
    } = this.props;
    PipelineStore.loadEnvData(projectId);
    this.loadData(page);
  };

  linkToChange = (path) => {
    const {
      history,
      AppState: {
        currentMenuType: {
          name,
          type,
          id: projectId,
          organizationId,
        },
      },
    } = this.props;

    const url = `${path}?type=${type}&id=${projectId}&name=${encodeURIComponent(name)}&organizationId=${organizationId}`;
    history.push(url);
  };

  tableChange = ({ current, pageSize }, filters, sorter, param) => {
    const {
      PipelineStore,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const { searchData, envIds } = this.state;

    const realSorter = _.isEmpty(sorter) ? null : sorter;
    this.setState({
      page: current,
      pageSize,
      param,
      filters,
      sorter: realSorter,
    });
    PipelineStore.loadListData(
      projectId,
      current,
      pageSize,
      realSorter,
      {
        searchParam: filters,
        param: param.toString(),
      },
      searchData,
      envIds,
    );
  };

  /**
   * 加载数据
   * @param toPage 指定加载页码
   */
  loadData = (toPage) => {
    const {
      PipelineStore,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;
    const { page, pageSize, param, filters, sorter, searchData, envIds } = this.state;
    const currentPage = toPage || page;
    const {
      getPageInfo: {
        pageSize: storePageSize,
      },
    } = PipelineStore;

    PipelineStore.loadListData(
      projectId,
      currentPage,
      pageSize || storePageSize,
      sorter,
      {
        searchParam: filters,
        param: param.toString(),
      },
      searchData,
      envIds,
    );
  };

  /****************** 删除 *********************/

  openRemove(id, name) {
    this.setState({
      showDelete: true,
      deleteName: name,
      deleteId: id,
    });
  }

  closeRemove = () => {
    this.setState({ deleteId: null, deleteName: '', showDelete: false });
  };

  handleDelete = async () => {
    const {
      PipelineStore,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    const { deleteId } = this.state;
    this.setState({ deleteLoading: true });
    const response = await PipelineStore.deletePipeline(projectId, deleteId)
      .catch(e => {
        this.setState({ deleteLoading: false });
        Choerodon.handleResponseError(e);
      });

    if (handleCheckerProptError(response)) {
      this.closeRemove();
      this.handleRefresh(null, 1);
    }
    this.setState({ deleteLoading: false });
  };

  /*************** 启用、停用 *****************/

  renderStatus = (record) => {
    const { intl: { formatMessage } } = this.props;
    return <StatusTags
      name={formatMessage({ id: record ? 'active' : 'stop' })}
      color={record ? '#00bfa5' : '#cecece'}
    />;
  };

  makeStatusInvalid = async () => {
    const {
      PipelineStore,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    const { invalidId } = this.state;

    this.setState({ invalidLoading: true });
    const response = await PipelineStore
      .changeStatus(projectId, invalidId, STATUS_INVALID)
      .catch(e => {
        this.setState({ invalidLoading: false });
        Choerodon.handleResponseError(e);
      });
    if (handleCheckerProptError(response)) {
      this.closeInvalid();
      this.handleRefresh();
    }
    this.setState({ invalidLoading: false });
  };

  async makeStatusActive(id) {
    const {
      PipelineStore,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    const response = await PipelineStore
      .changeStatus(projectId, id, STATUS_ACTIVE)
      .catch(e => Choerodon.handleResponseError(e));

    if (handleCheckerProptError(response)) {
      this.handleRefresh();
    }
  }

  openInvalid(id, name) {
    this.setState({
      showInvalid: true,
      invalidId: id,
      invalidName: name,
    });
  };

  closeInvalid = () => {
    this.setState({
      showInvalid: false,
      invalidId: null,
      invalidName: '',
    });
  };

  /**
   * 执行流水线
   * @returns {Promise<void>}
   */
  executeFun = async () => {
    const {
      PipelineStore,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    const { executeId } = this.state;

    this.closeExecuteCheck();

    this.setState({ executeLoading: true });
    let response = await PipelineStore
      .executePipeline(projectId, executeId)
      .catch(e => Choerodon.handleResponseError(e));
    this.setState({ executeLoading: true });

    if (response && response.failed) {
      Choerodon.prompt(response.message);
    } else {
      this.linkToRecord(executeId);
    }
  };

  /**
   * 检测是否满足执行条件
   * @param name
   * @param id
   * @returns {Promise<void>}
   */
  async openExecuteCheck(id, name) {
    const {
      PipelineStore,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    this.setState({
      showExecute: true,
      executeName: name,
      executeId: id,
    });
    let response = await PipelineStore
      .checkExecute(projectId, id)
      .catch(e => Choerodon.handleResponseError(e));

    if (response && response.failed) {
      Choerodon.prompt(response.message);
      this.setState({ executeCheck: false });
      return;
    }
    if (response && response.permission && response.versions) {
      this.setState({ executeCheck: EXECUTE_PASS });
      return;
    }
    this.setState({ executeCheck: EXECUTE_FAILED, executeEnv: response.envName });
  }

  closeExecuteCheck = () => {
    this.setState({ showExecute: false, executeName: '', executeId: null, executeCheck: false, executeEnv: null });
  };

  /**
   * 跳转到创建页面
   */
  linkToCreate = () => {
    const { match } = this.props;
    this.linkToChange(`${match.url}/create`);
  };

  /**
   * 跳转到执行详情页面
   */
  linkToRecord(id) {
    const {
      history,
      AppState: {
        currentMenuType: {
          projectId,
          name: projectName,
          organizationId,
          type,
        },
      },
    } = this.props;
    history.push({
      pathname: '/devops/pipeline-record',
      search: `?type=${type}&id=${projectId}&name=${projectName}&organizationId=${organizationId}`,
      state: { pipelineId: id, fromPipeline: true },
    });
  };

  /**
   * 跳转到编辑页面
   * @param id
   */
  linkToEdit(id) {
    const { match } = this.props;
    this.linkToChange(`${match.url}/edit/${id}`);
  }

  /**
   * 快速搜索,选择部署环境
   * @param value
   */
  handleSearch = (value, type) => {
    this.setState({ [type]: value }, () => this.loadData(1));
  };

  renderAction = (record) => {
    const {
      intl: { formatMessage },
    } = this.props;
    const { id, name, isEnabled, triggerType, execute, edit } = record;

    const _filterItem = (collection, predicate) => _.filter(collection, item => {
      return Array.isArray(predicate) ? !_.includes(predicate, item) : item !== predicate;
    });

    let action = {
      detail: {
        service: ['devops-service.pipeline.listRecords'],
        text: formatMessage({ id: 'pipeline.action.detail' }),
        action: this.linkToRecord.bind(this, id),
      },
      execute: {
        service: ['devops-service.pipeline.execute'],
        text: formatMessage({ id: 'pipeline.action.run' }),
        action: this.openExecuteCheck.bind(this, id, name),
      },
      edit: {
        service: ['devops-service.pipeline.update'],
        text: formatMessage({ id: 'edit' }),
        action: this.linkToEdit.bind(this, id),
      },
      disabled: {
        service: ['devops-service.pipeline.updateIsEnabled'],
        text: formatMessage({ id: 'stop' }),
        action: this.openInvalid.bind(this, id, name),
      },
      enable: {
        service: ['devops-service.pipeline.updateIsEnabled'],
        text: formatMessage({ id: 'active' }),
        action: this.makeStatusActive.bind(this, id),
      },
      remove: {
        service: ['devops-service.pipeline.delete'],
        text: formatMessage({ id: 'delete' }),
        action: this.openRemove.bind(this, id, name),
      },
    };

    let actionItem = _.keys(action);
    actionItem = _filterItem(actionItem, isEnabled ? 'enable' : 'disabled');

    if (triggerType === 'auto' || !execute) {
      actionItem = _filterItem(actionItem, 'execute');
    }

    if (!edit) {
      actionItem = _filterItem(actionItem, ['edit', 'remove']);
    }

    // 停用的流水线不能修改
    if (!isEnabled) {
      actionItem = _filterItem(actionItem, 'edit');
    }

    return (<Action data={_.map(actionItem, item => ({ ...action[item] }))} />);
  };

  get getColumns() {
    const { filters, sorter } = this.state;
    const { columnKey, order } = sorter || {};

    return [{
      width: 90,
      title: <FormattedMessage id="status" />,
      key: 'isEnabled',
      dataIndex: 'isEnabled',
      sorter: true,
      sortOrder: columnKey === 'isEnabled' && order,
      filters: [],
      filteredValue: filters.isEnabled || [],
      render: this.renderStatus,
    }, {
      title: <FormattedMessage id="pipeline.trigger" />,
      key: 'triggerType',
      dataIndex: 'triggerType',
      sorter: true,
      sortOrder: columnKey === 'triggerType' && order,
      filters: [],
      filteredValue: filters.triggerType || [],
      render: _renderTrigger,
    }, {
      title: <FormattedMessage id="name" />,
      key: 'name',
      dataIndex: 'name',
      sorter: true,
      sortOrder: columnKey === 'name' && order,
      filters: [],
      filteredValue: filters.name || [],
    }, {
      title: <FormattedMessage id="creator" />,
      key: 'createUserRealName',
      render: _renderUser,
    }, {
      title: <FormattedMessage id="updateDate" />,
      key: 'lastUpdateDate',
      dataIndex: 'lastUpdateDate',
      sorter: true,
      sortOrder: columnKey === 'lastUpdateDate' && order,
      render: _renderDate,
    }, {
      key: 'action',
      align: 'right',
      width: 60,
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
      PipelineStore: {
        getListData,
        getPageInfo,
        getLoading,
        getEnvData,
      },
    } = this.props;
    const {
      param,
      showDelete,
      deleteName,
      deleteLoading,
      showInvalid,
      invalidName,
      invalidLoading,
      showExecute,
      executeName,
      executeCheck,
      executeLoading,
      executeEnv,
    } = this.state;

    return (<Page
      className="c7n-region"
      service={[
        'devops-service.pipeline.create',
        'devops-service.pipeline.update',
        'devops-service.pipeline.listByOptions',
        'devops-service.pipeline.updateIsEnabled',
        'devops-service.pipeline.delete',
        'devops-service.pipeline.execute',
        'devops-service.pipeline.listRecords',
        'devops-service.pipeline.checkDeploy',
      ]}
    >
      <Header title={<FormattedMessage id="pipeline.head" />}>
        <Permission
          service={['devops-service.pipeline.create']}
          type={type}
          projectId={projectId}
          organizationId={organizationId}
        >
          <Button
            funcType="flat"
            icon="playlist_add"
            onClick={this.linkToCreate}
          >
            <FormattedMessage id="pipeline.header.create" />
          </Button>
        </Permission>
        <Button
          icon='refresh'
          onClick={this.handleRefresh}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </Header>
      <Content code="pipeline" values={{ name }}>
        <Select
          mode="multiple"
          label={formatMessage({ id: 'pipeline.search' })}
          allowClear
          className="c7ncd-pipeline-search"
          onChange={value => this.handleSearch(value, 'searchData')}
          choiceRemove={false}
        >
          {
            _.map(FAST_SEARCH, item => (
              <Option
                key={item}
                value={item}
              >
                {formatMessage({ id: `pipeline.search.${item}` })}
              </Option>
            ))
          }
        </Select>
        <Select
          mode="multiple"
          className='c7ncd-pipeline-search'
          label={formatMessage({ id: 'pipeline.deploy.env' })}
          optionFilterProp="children"
          filter
          allowClear
          choiceRemove={false}
          onChange={value => this.handleSearch(value, 'envIds')}
          filterOption={(input, option) =>
            option.props.children
              .toLowerCase()
              .indexOf(input.toLowerCase()) >= 0
          }
        >
          {_.map(getEnvData, ({ name, id }) => (
            <Option
              key={id}
              value={id}
            >
              {name}
            </Option>
          ))}
        </Select>
        <Table
          filterBarPlaceholder={formatMessage({ id: 'filter' })}
          loading={getLoading || executeLoading}
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
        title={`${formatMessage({ id: 'pipeline.delete' })}“${deleteName}”`}
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
          <FormattedMessage id="pipeline.delete.message" />
        </div>
      </Modal>)}
      {showExecute && (<Modal
        visible={showExecute}
        title={`${formatMessage({ id: 'pipeline.execute' })}“${executeName}”`}
        closable={false}
        footer={executeCheck === EXECUTE_PASS ? [
          <Button key="back" onClick={this.closeExecuteCheck}>
            <FormattedMessage id="cancel" />
          </Button>,
          <Button
            key="submit"
            type="primary"
            onClick={this.executeFun}
          >
            <FormattedMessage id="submit" />
          </Button>,
        ] : [<Button key="back" onClick={this.closeExecuteCheck}>
          <FormattedMessage id="close" />
        </Button>]}
      >
        <div className="c7n-padding-top_8">
          {executeCheck
            ? (executeEnv
                ? <FormattedMessage
                  id={`pipeline.execute.no.permission`}
                  values={{ envName: executeEnv }}
                />
                : <FormattedMessage id={`pipeline.execute.${executeCheck}`} />
            )
            : <Fragment>
              <Spin size="small" />
              <span className="c7ncd-pipeline-execute">{formatMessage({ id: 'pipeline.execute.checking' })}</span>
            </Fragment>
          }
        </div>
      </Modal>)}
      {showInvalid && (<Modal
        visible={showInvalid}
        title={`${formatMessage({ id: 'pipeline.invalid' })}“${invalidName}”`}
        closable={false}
        footer={[
          <Button key="back" onClick={this.closeInvalid} disabled={invalidLoading}>
            <FormattedMessage id="cancel" />
          </Button>,
          <Button
            key="submit"
            type="danger"
            onClick={this.makeStatusInvalid}
            loading={invalidLoading}
          >
            <FormattedMessage id="submit" />
          </Button>,
        ]}
      >
        <div className="c7n-padding-top_8">
          <FormattedMessage id="pipeline.invalid.message" />
        </div>
      </Modal>)}
    </Page>);
  }
}

function _renderTrigger(data) {
  return <FormattedMessage id={`pipeline.trigger.${data}`} />;
}

function _renderDate(record) {
  return <TimePopover content={record} />;
}

function _renderUser({ createUserRealName, createUserName, createUserUrl }) {
  return <UserInfo avatar={createUserUrl} name={createUserRealName} id={createUserName} />;
}
