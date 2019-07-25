import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Table, Button, Tooltip, Popover } from 'choerodon-ui';
import { Permission } from '@choerodon/boot';
import _ from 'lodash';
import MouserOverWrapper from '../../../components/MouseOverWrapper';
import TimePopover from '../../../components/timePopover';
import StatusTags from '../../../components/StatusTags';
import EnvOverviewStore from '../../envOverview/stores';
import DeleteModal from '../../../components/deleteModal';
import { handleProptError } from '../../../utils';

@injectIntl
@inject('AppState')
@observer
export default class KeyValueTable extends Component {
  state = {
    deleteLoading: false,
    deleteArr: [],
  };

  handleDelete = async (id, callback) => {
    const {
      store,
      envId,
      title,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;

    const {
      getPageInfo: {
        current,
        pageSize,
      },
    } = store;

    const deleteFuncMap = {
      configMap: async () => await store.deleteConfigMap(projectId, id),
      secret: async () => await store.deleteSecret(projectId, id, envId),
    };

    const loadDataMap = {
      configMap: () => store.loadConfigMap(true, projectId, envId, current, pageSize),
      secret: () => store.loadSecret(true, projectId, envId, current, pageSize),
    };

    this.setState({ deleteLoading: true });

    const response = await deleteFuncMap[title]()
      .catch(e => {
        this.setState({ deleteLoading: false });
        callback && callback();
        Choerodon.handleResponseError(e);
      });

    const result = handleProptError(response);

    if (result) {
      this.removeDeleteModal(id);
      loadDataMap[title]();
    }

    this.setState({ deleteLoading: false });
  };

  /**
   * 表格筛选排序等
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  tableChange = (pagination, filters, sorter, paras) => {
    const {
      store,
      envId,
      title,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;

    store.setInfo({ filters, sort: sorter, paras });

    let sort = { field: '', order: 'desc' };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === 'ascend') {
        sort.order = 'asc';
      } else if (sorter.order === 'descend') {
        sort.order = 'desc';
      }
    }
    let page = pagination.current;
    let searchParam = {};
    if (Object.keys(filters).length) {
      searchParam = filters;
    }
    const postData = {
      searchParam,
      param: paras.toString(),
    };
    if (title === 'configMap') {
      store.loadConfigMap(true, projectId, envId, page, pagination.pageSize, sort, postData);
    } else if (title === 'secret') {
      store.loadSecret(true, projectId, envId, page, pagination.pageSize, sort, postData);
    }
  };

  /**
   * 显示删除确认框
   * @param id
   * @param name
   */
  openRemoveModal = (id, name) => {
    const deleteArr = [...this.state.deleteArr];

    const currentIndex = _.findIndex(deleteArr, item => id === item.deleteId);

    if (~currentIndex) {
      const newItem = {
        ...deleteArr[currentIndex],
        display: true,
      };
      deleteArr.splice(currentIndex, 1, newItem);
    } else {
      deleteArr.push({
        display: true,
        deleteId: id,
        name,
      });
    }

    this.setState({ deleteArr });
  };

  /**
   * 关闭删除弹窗
   */
  closeRemoveModal = (id) => {
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

  renderStatus = ({ commandStatus }) => <StatusTags
    name={this.props.intl.formatMessage({ id: commandStatus })}
    colorCode={commandStatus}
    style={{ minWidth: 40 }}
  />;

  renderName = ({ name, description }) => <MouserOverWrapper width={0.3}>
    <Popover
      overlayStyle={{ maxWidth: '350px', wordBreak: 'break-word' }}
      placement="topLeft"
      content={`${this.props.intl.formatMessage({ id: 'ist.des' })}${description}`}
    >
      {name}
    </Popover>
  </MouserOverWrapper>;

  renderKey = (text) => <MouserOverWrapper width={0.5}>
    <Popover
      content={text.join(',')}
      placement="topLeft"
      overlayStyle={{ maxWidth: '350px', wordBreak: 'break-word' }}
    >
      {text.join(',')}
    </Popover>
  </MouserOverWrapper>;

  renderActions = ({ id, commandStatus, name }) => {
    const {
      envId,
      editOpen,
      AppState: {
        currentMenuType: {
          id: projectId,
          type,
          organizationId,
        },
      },
    } = this.props;

    const envData = EnvOverviewStore.getEnvcard;
    const envState = envData.length
      ? envData.filter(d => d.id === Number(envId))[0]
      : { connect: false };

    return <Fragment>
      <Permission
        type={type}
        projectId={projectId}
        organizationId={organizationId}
        service={[
          'devops-service.devops-config-map.create',
          'devops-service.devops-secret.createOrUpdate',
        ]}
      >
        <Tooltip
          placement="bottom"
          title={envState && !envState.connect
            ? <FormattedMessage id="envoverview.envinfo" />
            : <FormattedMessage id="edit" />}
        >
          <Button
            disabled={commandStatus === 'operating' || (envState && !envState.connect)}
            icon="mode_edit"
            shape="circle"
            size="small"
            onClick={() => editOpen(id)}
          />
        </Tooltip>
      </Permission>
      <Permission
        type={type}
        projectId={projectId}
        organizationId={organizationId}
        service={[
          'devops-service.devops-config-map.delete',
          'devops-service.devops-secret.deleteSecret',
        ]}>
        <Tooltip
          placement="bottom"
          title={envState && !envState.connect
            ? <FormattedMessage id="envoverview.envinfo" />
            : <FormattedMessage id="delete" />}
        >
          <Button
            disabled={commandStatus === 'operating' || (envState && !envState.connect)}
            icon="delete_forever"
            shape="circle"
            size="small"
            onClick={this.openRemoveModal.bind(this, id, name)}
          />
        </Tooltip>
      </Permission>
    </Fragment>;
  };

  getColumns = () => {
    const { store } = this.props;
    const {
      filters,
      sort: {
        columnKey,
        order,
      },
    } = store.getInfo;

    return [
      {
        title: <FormattedMessage id="app.active" />,
        key: 'status',
        width: 90,
        render: this.renderStatus,
      }, {
        title: <FormattedMessage id="app.name" />,
        key: 'name',
        sorter: true,
        sortOrder: columnKey === 'name' && order,
        filters: [],
        filteredValue: filters.name || [],
        render: this.renderName,
      }, {
        title: <FormattedMessage id="configMap.key" />,
        dataIndex: 'key',
        key: 'key',
        render: this.renderKey,
      }, {
        title: <FormattedMessage id="configMap.updateAt" />,
        dataIndex: 'lastUpdateDate',
        key: 'createdAt',
        render: text => <TimePopover content={text} />,
      }, {
        align: 'right',
        width: 104,
        key: 'action',
        render: this.renderActions,
      }];
  };

  render() {
    const {
      intl: { formatMessage },
      store,
      title,
    } = this.props;
    const {
      deleteLoading,
      deleteArr,
    } = this.state;
    const { paras } = store.getInfo;

    const columns = this.getColumns();

    const modalTitle = formatMessage({ id: `${title}.delete` });
    const deleteModals = _.map(deleteArr, ({ name, display, deleteId }) => (<DeleteModal
      key={deleteId}
      title={`${modalTitle}“${name}”`}
      visible={display}
      objectId={deleteId}
      loading={deleteLoading}
      objectType={title}
      onClose={this.closeRemoveModal}
      onOk={this.handleDelete}
    />));

    return (
      <Fragment>
        <Table
          filterBarPlaceholder={formatMessage({ id: 'filter' })}
          loading={store.loading}
          pagination={store.getPageInfo}
          columns={columns}
          filters={paras.slice()}
          dataSource={store.getData}
          rowKey={record => record.id}
          onChange={this.tableChange}
        />
        {deleteModals}
      </Fragment>
    );
  }
}
