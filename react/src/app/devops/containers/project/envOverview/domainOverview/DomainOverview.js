/* eslint-disable react/sort-comp */
import React, { Component } from "react";
import { observer } from "mobx-react";
import { observable, action } from "mobx";
import { withRouter } from "react-router-dom";
import { injectIntl, FormattedMessage } from "react-intl";
import { Table, Button, Form, Tooltip, Modal, Progress } from "choerodon-ui";
import { Permission, stores } from "@choerodon/boot";
import _ from "lodash";
import "../EnvOverview.scss";
import "../../domain/domainHome/DomainHome.scss";
import "../../../main.scss";
import DomainStore from "../../../../stores/project/domain";
import CreateDomain from "../../domain/createDomain";
import StatusIcon from "../../../../components/StatusIcon";

const { AppState } = stores;

@observer
class DomainOverview extends Component {
  @observable openRemove = false;

  @observable showDomain = false;

  @observable submitting = false;

  constructor(props, context) {
    super(props, context);
    this.state = {};
  }

  /**
   * 按环境加载域名
   * @param envId
   */
  loadDomain = (envId, spin = true) => {
    const { store } = this.props;
    const projectId = AppState.currentMenuType.id;
    store.loadDomain(spin, projectId, envId);
  };

  /**
   * 删除数据
   */
  @action
  handleDelete = () => {
    const { store, envId } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const { total, current, pageSize } = store.getPageInfo;
    const lastDatas = total % pageSize;
    const totalPage = Math.ceil(total / pageSize);
    this.submitting = true;
    DomainStore.deleteData(projectId, this.id)
      .then(() => {
        this.submitting = false;
        if (lastDatas === 1 && current === totalPage && current > 1) {
          store.loadDomain(
            true,
            projectId,
            envId,
            current - 2
          );
        } else {
          store.loadDomain(
            true,
            projectId,
            envId,
            current - 1
          );
        }
        this.closeRemove();
      })
      .catch(error => {
        this.submitting = false;
        Choerodon.handleResponseError(error);
      });
    store.setInfo({
      filters: {},
      sort: { columnKey: "id", order: "descend" },
      paras: [],
    });
  };

  /**
   *打开域名编辑弹框
   */
  @action
  createDomain = (type, id = "") => {
    this.props.form.resetFields();
    if (type === "create") {
      this.domainTitle = this.props.intl.formatMessage({
        id: "domain.header.create",
      });
      this.domainType = type;
      this.domainId = id;
    } else {
      this.domainTitle = this.props.intl.formatMessage({
        id: "domain.header.update",
      });
      this.domainType = type;
      this.domainId = id;
    }
    this.showDomain = true;
  };

  /**
   * 打开删除网络弹框
   * @param id
   */
  @action
  openRemoveDomain = id => {
    this.openRemove = true;
    this.id = id;
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
        sort: { columnKey: "id", order: "descend" },
        paras: [],
      });
    }
  };

  /**
   * 关闭删除数据的模态框
   */
  @action
  closeRemove = () => {
    this.openRemove = false;
  };

  /**
   * table 操作
   * @param pagination
   * @param filters
   * @param sorter
   * @param paras
   */
  tableChange = (pagination, filters, sorter, paras) => {
    const { store, envId } = this.props;
    const { id } = AppState.currentMenuType;
    const sort = { field: "", order: "desc" };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === "ascend") {
        sort.order = "asc";
      } else if (sorter.order === "descend") {
        sort.order = "desc";
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
    store.loadDomain(
      true,
      id,
      envId,
      page,
      pagination.pageSize,
      sort,
      postData
    );
  };

  render() {
    const { intl, store, envId } = this.props;
    const data = store.getDomain;
    const {
      filters,
      sort: { columnKey, order },
      paras,
    } = store.getInfo;
    const menu = AppState.currentMenuType;
    const { type, id: projectId, organizationId: orgId } = menu;
    const columns = [
      {
        title: intl.formatMessage({ id: "domain.column.name" }),
        key: "name",
        sorter: true,
        sortOrder: columnKey === "name" && order,
        filters: [],
        filteredValue: filters.name || [],
        render: record => (
          <StatusIcon
            name={record.name}
            status={record.commandStatus || ""}
            error={record.error || ""}
          />
        ),
      },
      {
        title: intl.formatMessage({ id: "domain.column.domain" }),
        key: "domain",
        filters: [],
        filteredValue: filters.domain || [],
        dataIndex: "domain",
      },
      {
        title: intl.formatMessage({ id: "domain.column.path" }),
        className: "c7n-network-col",
        key: "path",
        sorter: true,
        sortOrder: columnKey === "path" && order,
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
        title: intl.formatMessage({ id: "domain.column.network" }),
        className: "c7n-network-col",
        key: "serviceName",
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
                  title={intl.formatMessage({
                    id: `${instance.serviceStatus || "null"}`,
                  })}
                  placement="top"
                >
                  <span
                    className={`c7ncd-status c7ncd-status-${
                      instance.serviceStatus === "running"
                        ? "success"
                        : "disconnect"
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
        key: "action",
        align: "right",
        className: "c7n-network-text_top",
        render: record => {
          let editDom = null;
          let deletDom = null;
          switch (record.status) {
            case "operating":
              editDom = (
                <Tooltip
                  trigger="hover"
                  placement="bottom"
                  title={intl.formatMessage({ id: `domain_${record.status}` })}
                >
                  <i className="icon icon-mode_edit c7n-app-icon-disabled" />
                </Tooltip>
              );
              deletDom = (
                <Tooltip
                  trigger="hover"
                  placement="bottom"
                  title={intl.formatMessage({ id: `domain_${record.status}` })}
                >
                  <i className="icon icon-delete_forever c7n-app-icon-disabled" />
                </Tooltip>
              );
              break;
            default:
              editDom = (
                <React.Fragment>
                  {record.envStatus ? (
                    <Tooltip
                      trigger="hover"
                      placement="bottom"
                      title={<div>{intl.formatMessage({ id: "edit" })}</div>}
                    >
                      <Button
                        shape="circle"
                        size="small"
                        funcType="flat"
                        onClick={this.createDomain.bind(
                          this,
                          "edit",
                          record.id
                        )}
                      >
                        <i className="icon icon-mode_edit" />
                      </Button>
                    </Tooltip>
                  ) : (
                    <Tooltip
                      trigger="hover"
                      placement="bottom"
                      title={
                        <div>
                          {intl.formatMessage({ id: "network.env.tooltip" })}
                        </div>
                      }
                    >
                      <i className="icon icon-mode_edit c7n-app-icon-disabled" />
                    </Tooltip>
                  )}
                </React.Fragment>
              );
              deletDom = (
                <React.Fragment>
                  {record.envStatus ? (
                    <Tooltip
                      trigger="hover"
                      placement="bottom"
                      title={<div>{intl.formatMessage({ id: "delete" })}</div>}
                    >
                      <Button
                        shape="circle"
                        size="small"
                        funcType="flat"
                        onClick={this.openRemoveDomain.bind(this, record.id)}
                      >
                        <i className="icon icon-delete_forever" />
                      </Button>
                    </Tooltip>
                  ) : (
                    <Tooltip
                      trigger="hover"
                      placement="bottom"
                      title={
                        <div>
                          {intl.formatMessage({ id: "network.env.tooltip" })}
                        </div>
                      }
                    >
                      <i className="icon icon-delete_forever c7n-app-icon-disabled" />
                    </Tooltip>
                  )}
                </React.Fragment>
              );
          }
          return (
            <div>
              <Permission
                service={["devops-service.devops-ingress.update"]}
                type={type}
                projectId={projectId}
                organizationId={orgId}
              >
                {editDom}
              </Permission>
              <Permission
                service={["devops-service.devops-ingress.delete"]}
                type={type}
                projectId={projectId}
                organizationId={orgId}
              >
                {deletDom}
              </Permission>
            </div>
          );
        },
      },
    ];

    return (
      <div className="c7n-domain-wrapper">
        <Table
          filterBarPlaceholder={intl.formatMessage({ id: "filter" })}
          loading={store.isLoading}
          onChange={this.tableChange}
          pagination={store.pageInfo}
          columns={columns}
          dataSource={data}
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
        <Modal
          visible={this.openRemove}
          title={<FormattedMessage id="domain.header.delete" />}
          closable={false}
          footer={[
            <Button
              key="back"
              onClick={this.closeRemove}
              disabled={this.submitting}
            >
              {<FormattedMessage id="cancel" />}
            </Button>,
            <Button
              key="submit"
              type="danger"
              onClick={this.handleDelete}
              loading={this.submitting}
            >
              {intl.formatMessage({ id: "delete" })}
            </Button>,
          ]}
        >
          <div className="c7n-padding-top_8">
            {intl.formatMessage({ id: "confirm.delete" })}
          </div>
        </Modal>
      </div>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(DomainOverview)));
