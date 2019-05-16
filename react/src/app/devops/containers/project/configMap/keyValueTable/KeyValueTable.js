import React, { Component, Fragment } from "react";
import { observer } from "mobx-react";
import { injectIntl, FormattedMessage } from "react-intl";
import { Table, Button, Tooltip, Popover, Modal } from "choerodon-ui";
import { Permission, stores } from "@choerodon/boot";
import MouserOverWrapper from "../../../../components/MouseOverWrapper";
import TimePopover from '../../../../components/timePopover';
import StatusTags from '../../../../components/StatusTags';
import EnvOverviewStore from "../../../../stores/project/envOverview";

const { AppState } = stores;

@observer
class KeyValueTable extends Component {
  constructor(props) {
    super(props);
    this.state = {
      deleteStatus: false,
      removeDisplay: false,
      delName: '',
      delId: false,
    };
  }

  deleteKeyValue = () => {
    const { store, envId, title } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const { delId } = this.state;
    const datas = store.getData;
    this.setState({ deleteStatus: true });
    if (title === 'configMap') {
      store.deleteConfigMap(projectId, delId)
        .then((data) => {
          const pagination = store.getPageInfo;
          let page = pagination.current - 1;
          if (data && data.failed) {
            Choerodon.prompt(data.message);
            this.setState({
              deleteStatus: false,
            })
          } else {
            this.setState({
              delId: null,
              removeDisplay: false,
              deleteStatus: false,
            }, () => {
              if (datas.length % this.state.size === 1) {
                store.loadConfigMap(true, projectId, envId, page - 1, pagination.pageSize);
              } else {
                store.loadConfigMap(true, projectId, envId, page, pagination.pageSize);
              }
            })
          }
        })
        .catch(e => {
          this.setState({
            deleteStatus: false,
          });
          Choerodon.handleResponseError(e);
        });
    } else if (title === 'secret') {
      store.deleteSecret(projectId, delId, envId)
        .then((data) => {
          const pagination = store.getPageInfo;
          let page = pagination.current - 1;
          if (data && data.failed) {
            Choerodon.prompt(data.message);
            this.setState({
              deleteStatus: false,
            })
          } else {
            this.setState({
              delId: null,
              removeDisplay: false,
              deleteStatus: false,
            }, () => {
              if (datas.length % this.state.size === 1) {
                store.loadSecret(true, projectId, envId, page - 1, pagination.pageSize);
              } else {
                store.loadSecret(true, projectId, envId, page, pagination.pageSize);
              }
            })
          }
        })
        .catch(e => {
          this.setState({
            deleteStatus: false,
          });
          Choerodon.handleResponseError(e);
        });
    }
  };

  /**
   * 表格筛选排序等
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  tableChange = (pagination, filters, sorter, paras) => {
    const { store, envId, title } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    store.setInfo({ filters, sort: sorter, paras });
    let sort = { field: '', order: 'desc' };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if(sorter.order === 'ascend') {
        sort.order = 'asc';
      } else if(sorter.order === 'descend'){
        sort.order = 'desc';
      }
    }
    let page = pagination.current - 1;
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
  openRemove = (id, name) => {
    this.setState({
      removeDisplay: true,
      delId: id,
      delName: name,
    });
  };

  /**
   * 关闭删除弹窗
   */
  closeRemoveModal = () => this.setState({ removeDisplay: false });

  render() {
    const {
      intl: { formatMessage }, store, envId, title } = this.props;
    const { removeDisplay, deleteStatus, delName } = this.state;
    const { filters, sort: { columnKey, order }, paras } = store.getInfo;
    const {
      type,
      id: projectId,
      organizationId,
    } = AppState.currentMenuType;
    const data = store.getData;
    const envData = EnvOverviewStore.getEnvcard;
    const envState = envData.length
      ? envData.filter(d => d.id === Number(envId))[0]
      : { connect: false };

    const columns = [
      {
        title: <FormattedMessage id="app.active" />,
        key: 'status',
        render: record => <StatusTags name={formatMessage({ id: record.commandStatus })} colorCode={record.commandStatus} />,
      },{
        title: <FormattedMessage id="app.name" />,
        key: 'name',
        sorter: true,
        sortOrder: columnKey === 'name' && order,
        filters: [],
        filteredValue: filters.name || [],
        render: record => (<MouserOverWrapper width={0.3}>
          <Popover overlayStyle={{ maxWidth: '350px', wordBreak: 'break-word' }} placement="topLeft" content={`${formatMessage({ id: "ist.des" })}${record.description}`}>
            {record.name}
          </Popover>
        </MouserOverWrapper>),
      }, {
        title: <FormattedMessage id="configMap.key" />,
        dataIndex: 'key',
        key: 'key',
        render: text => (<MouserOverWrapper width={0.5}>
          <Popover content={text.join(',')} placement="topLeft" overlayStyle={{ maxWidth: '350px', wordBreak: 'break-word' }}>
              {text.join(',')}
          </Popover>
        </MouserOverWrapper>),
      }, {
        title: <FormattedMessage id="configMap.updateAt" />,
        dataIndex: 'lastUpdateDate',
        key: 'createdAt',
        render: text => <TimePopover content={text} />,
      }, {
        align: 'right',
        width: 104,
        key: 'action',
        render: record => (
          <Fragment>
            <Permission type={type} projectId={projectId} organizationId={organizationId} service={['devops-service.devops-config-map.create', 'devops-service.devops-secret.createOrUpdate']}>
              <Tooltip
                placement="bottom"
                title={envState && !envState.connect ? <FormattedMessage id="envoverview.envinfo" /> : <FormattedMessage id="edit" />}
              >
                <Button
                  disabled={record.commandStatus === 'operating' || (envState && !envState.connect)}
                  icon="mode_edit"
                  shape="circle"
                  size="small"
                  onClick={this.props.editOpen.bind(this, record.id)}
                />
              </Tooltip>
            </Permission>
            <Permission type={type} projectId={projectId} organizationId={organizationId} service={['devops-service.devops-config-map.delete', 'devops-service.devops-secret.deleteSecret']}>
              <Tooltip
                placement="bottom"
                title={envState && !envState.connect ? <FormattedMessage id="envoverview.envinfo" /> : <FormattedMessage id="delete" />}
              >
                <Button
                  disabled={record.commandStatus === 'operating' || (envState && !envState.connect)}
                  icon="delete_forever"
                  shape="circle"
                  size="small"
                  onClick={this.openRemove.bind(this, record.id, record.name)}
                />
              </Tooltip>
            </Permission>
          </Fragment>
        ),
      }];

    return (
      <Fragment>
        <Table
          filterBarPlaceholder={formatMessage({ id: 'filter' })}
          loading={store.loading}
          pagination={store.getPageInfo}
          columns={columns}
          filters={paras.slice()}
          dataSource={data}
          rowKey={record => record.id}
          onChange={this.tableChange}
        />
        <Modal
          confirmLoading={deleteStatus}
          visible={removeDisplay}
          title={`${formatMessage({ id: `${title}.del` })}“${delName}”`}
          closable={false}
          footer={[
            <Button
              key="back"
              onClick={this.closeRemoveModal}
              disabled={deleteStatus}
            >
              <FormattedMessage id="cancel" />
            </Button>,
            <Button
              key="submit"
              loading={deleteStatus}
              type="danger"
              onClick={this.deleteKeyValue}
            >
              <FormattedMessage id="delete" />
            </Button>,
          ]}
        >
          <div className="c7n-padding-top_8">
            <FormattedMessage id={ `${title}.del.tooltip`} />
          </div>
        </Modal>
      </Fragment>
    );
  }
}

export default injectIntl(KeyValueTable);
