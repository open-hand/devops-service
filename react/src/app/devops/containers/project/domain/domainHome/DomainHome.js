import React, { Component, Fragment } from "react";
import { observer } from "mobx-react";
import { withRouter } from "react-router-dom";
import { Table, Button, Form, Tooltip, Modal, Select } from "choerodon-ui";
import { injectIntl, FormattedMessage } from "react-intl";
import {
  Content,
  Header,
  Page,
  Permission,
  stores,
} from "@choerodon/boot";
import _ from "lodash";
import CreateDomain from "../createDomain";
import LoadingBar from "../../../../components/loadingBar";
import "./DomainHome.scss";
import "../../../main.scss";
import { commonComponent } from "../../../../components/commonFunction";
import StatusIcon from "../../../../components/StatusIcon";
import MouserOverWrapper from "../../../../components/MouseOverWrapper/MouserOverWrapper";
import EnvOverviewStore from "../../../../stores/project/envOverview";
import DepPipelineEmpty from "../../../../components/DepPipelineEmpty/DepPipelineEmpty";
import RefreshBtn from "../../../../components/refreshBtn";
import EnvFlag from "../../../../components/envFlag";

const { AppState } = stores;
const { Option } = Select;

@commonComponent("DomainStore")
@observer
class DomainHome extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
      openRemove: false,
      submitting: false,
      show: false,
    };
    this.opColumn = this.opColumn.bind(this);
  }

  componentDidMount() {
    const { id: projectId } = AppState.currentMenuType;
    EnvOverviewStore.loadActiveEnv(projectId).then(env => {
      if (env.length) {
        const envId = EnvOverviewStore.getTpEnvId;
        if (envId) {
          // 这个方法定义在 commonComponent装饰器中
          this.loadAllData(0, envId);
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
        sort: { columnKey: "id", order: "descend" },
        paras: [],
      });
      const envId = EnvOverviewStore.getTpEnvId;
      this.loadAllData(0, envId);
    }
  };

  /**
   *打开域名创建弹框
   */
  showSideBar = (type, id = "") => {
    const { form, DomainStore } = this.props;
    form.resetFields();
    DomainStore.setCertificates([]);
    if (type === "create") {
      this.setState({
        show: true,
        title: this.props.intl.formatMessage({ id: "domain.header.create" }),
        type,
        id,
      });
    } else {
      this.setState(
        {
          title: this.props.intl.formatMessage({ id: "domain.header.update" }),
          type,
          id,
        },
        () => {
          this.setState({ show: true });
        }
      );
    }
  };

  opColumn(record) {
    const {
      intl: { formatMessage },
    } = this.props;

    const { type, id: projectId, organizationId } = AppState.currentMenuType;

    let editDom = null;
    let deletDom = null;
    switch (record.status) {
      case "operating":
        editDom = (
          <Tooltip
            trigger="hover"
            placement="bottom"
            title={formatMessage({ id: `domain_${record.status}` })}
          >
            <i className="icon icon-mode_edit c7n-app-icon-disabled" />
          </Tooltip>
        );
        deletDom = (
          <Tooltip
            trigger="hover"
            placement="bottom"
            title={formatMessage({ id: `domain_${record.status}` })}
          >
            <i className="icon icon-delete_forever c7n-app-icon-disabled" />
          </Tooltip>
        );
        break;
      default:
        editDom = (
          <Fragment>
            {record.envStatus ? (
              <Tooltip
                trigger="hover"
                placement="bottom"
                title={<div>{formatMessage({ id: "edit" })}</div>}
              >
                <Button
                  shape="circle"
                  size="small"
                  funcType="flat"
                  onClick={this.showSideBar.bind(this, "edit", record.id)}
                >
                  <i className="icon icon-mode_edit" />
                </Button>
              </Tooltip>
            ) : (
              <Tooltip
                trigger="hover"
                placement="bottom"
                title={
                  <div>{formatMessage({ id: "network.env.tooltip" })}</div>
                }
              >
                <i className="icon icon-mode_edit c7n-app-icon-disabled" />
              </Tooltip>
            )}
          </Fragment>
        );
        deletDom = (
          <Fragment>
            {record.envStatus ? (
              <Tooltip
                trigger="hover"
                placement="bottom"
                title={<div>{formatMessage({ id: "delete" })}</div>}
              >
                <Button
                  shape="circle"
                  size="small"
                  funcType="flat"
                  onClick={this.openRemove.bind(this, record.id, record.name)}
                >
                  <i className="icon icon-delete_forever" />
                </Button>
              </Tooltip>
            ) : (
              <Tooltip
                trigger="hover"
                placement="bottom"
                title={
                  <div>{formatMessage({ id: "network.env.tooltip" })}</div>
                }
              >
                <i className="icon icon-delete_forever c7n-app-icon-disabled" />
              </Tooltip>
            )}
          </Fragment>
        );
    }
    return (
      <Fragment>
        <Permission
          service={["devops-service.devops-ingress.update"]}
          type={type}
          projectId={projectId}
          organizationId={organizationId}
        >
          {editDom}
        </Permission>
        <Permission
          service={["devops-service.devops-ingress.delete"]}
          type={type}
          projectId={projectId}
          organizationId={organizationId}
        >
          {deletDom}
        </Permission>
      </Fragment>
    );
  }

  /**
   * 环境选择
   * @param value
   */
  handleEnvSelect = value => {
    EnvOverviewStore.setTpEnvId(value);
    this.loadAllData(0, value);
  };

  render() {
    const {
      DomainStore,
      intl: { formatMessage },
    } = this.props;

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

    const {
      type,
      id: projectId,
      organizationId: orgId,
      name,
    } = AppState.currentMenuType;

    const columns = [
      {
        title: formatMessage({ id: "domain.column.name" }),
        key: "name",
        dataIndex: "name",
        sorter: true,
        sortOrder: columnKey === "name" && order,
        filters: [],
        filteredValue: filters.name || [],
        render: (text, record) => (
          <div className="c7n-network-service">
            <MouserOverWrapper text={text} width={0.16}>
              {text}
            </MouserOverWrapper>
            <StatusIcon
              name=""
              status={record.commandStatus || ""}
              error={record.error || ""}
            />
          </div>
        ),
      },
      {
        title: formatMessage({ id: "domain.column.domain" }),
        key: "domain",
        filters: [],
        filteredValue: filters.domain || [],
        dataIndex: "domain",
      },
      {
        title: formatMessage({ id: "domain.column.env" }),
        key: "envName",
        sorter: true,
        sortOrder: columnKey === "envName" && order,
        filters: [],
        filteredValue: filters.envName || [],
        render: record => (
          <EnvFlag status={record.envStatus} name={record.envName} />
        ),
      },
      {
        title: formatMessage({ id: "domain.column.path" }),
        className: "c7n-network-col",
        key: "path",
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
        title: formatMessage({ id: "domain.column.network" }),
        className: "c7n-network-col",
        key: "serviceName",
        filters: [],
        filteredValue: filters.serviceName || [],
        render: record => (
          <div>
            {_.map(record.pathList, instance => (
              <div
                className="c7n-network-col_border c7n-network-service"
                key={`${record.id}-${instance.path}-${instance.serviceId}`}
              >
                <div
                  className={`c7n-domain-create-status c7n-domain-create-status_${
                    instance.serviceStatus
                  }`}
                >
                  <div>{formatMessage({ id: instance.serviceStatus })}</div>
                </div>
                <MouserOverWrapper text={instance.serviceName} width={0.1}>
                  {instance.serviceName}
                </MouserOverWrapper>
              </div>
            ))}
          </div>
        ),
      },
      {
        key: "action",
        align: "right",
        className: "c7n-network-text_top",
        render: this.opColumn,
      },
    ];

    let mainContent = null;
    if (envData && envData.length && envId) {
      this.initAutoRefresh("domain");
      mainContent = (
        <Fragment>
          <Header title={formatMessage({ id: "domain.header.title" })}>
            <Select
              className={`${
                envId
                  ? "c7n-header-select"
                  : "c7n-header-select c7n-select_min100"
              }`}
              dropdownClassName="c7n-header-env_drop"
              placeholder={formatMessage({ id: "envoverview.noEnv" })}
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
              service={["devops-service.devops-ingress.create"]}
              type={type}
              projectId={projectId}
              organizationId={orgId}
            >
              <Tooltip
                title={
                  envState && !envState.connect ? (
                    <FormattedMessage id="envoverview.envinfo" />
                  ) : null
                }
              >
                <Button
                  funcType="flat"
                  disabled={envState && !envState.connect}
                  onClick={this.showSideBar.bind(this, "create", "")}
                >
                  <i className="icon icon-playlist_add icon" />
                  <FormattedMessage id="domain.header.create" />
                </Button>
              </Tooltip>
            </Permission>
            <Permission
              service={["devops-service.devops-ingress.listByEnv"]}
              type={type}
              projectId={projectId}
              organizationId={orgId}
            >
              <RefreshBtn name="domain" onFresh={this.handleRefresh} />
            </Permission>
          </Header>
          <Content code="domain" values={{ name }}>
            <Table
              filterBarPlaceholder={formatMessage({ id: "filter" })}
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
          title={formatMessage({ id: "domain.header.title" })}
          type="env"
        />
      );
    }

    return (
      <Page
        className="c7n-region c7n-domain-wrapper"
        service={[
          "devops-service.devops-ingress.create",
          "devops-service.devops-ingress.checkDomain",
          "devops-service.devops-ingress.checkName",
          "devops-service.devops-ingress.listByEnv",
          "devops-service.devops-ingress.queryDomainId",
          "devops-service.devops-ingress.update",
          "devops-service.devops-ingress.delete",
          "devops-service.devops-service.listByEnvId",
          "devops-service.devops-environment.listByProjectIdAndActive",
        ]}
      >
        {DomainStore.isRefresh ? <LoadingBar display /> : mainContent}

        {this.state.show && (
          <CreateDomain
            id={this.state.id}
            envId={envId}
            title={this.state.title}
            visible={this.state.show}
            type={this.state.type}
            store={DomainStore}
            onClose={this.handleCancelFun}
          />
        )}
        <Modal
          visible={this.state.openRemove}
          title={`${formatMessage({ id: "domain.header.delete" })}“${
            this.state.name
          }”`}
          closable={false}
          footer={[
            <Button
              key="back"
              onClick={this.closeRemove}
              disabled={this.state.submitting}
            >
              {<FormattedMessage id="cancel" />}
            </Button>,
            <Button
              key="submit"
              loading={this.state.submitting}
              type="danger"
              onClick={this.handleDelete}
            >
              {formatMessage({ id: "delete" })}
            </Button>,
          ]}
        >
          <div className="c7n-padding-top_8">
            {formatMessage({ id: "ingress.delete.message" })}
          </div>
        </Modal>
      </Page>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(DomainHome)));
